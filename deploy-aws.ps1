<#
.SYNOPSIS
    Deploys StreamForge production architecture to AWS and displays real-time terminal progress.
.DESCRIPTION
    Provisions Amazon S3, RDS PostgreSQL, Amazon MQ RabbitMQ, Secrets Manager, ECR, ALB, CloudFront,
    CloudWatch, and ECS Fargate.
#>

param(
    [string]$StackName = "streamforge-production-stack",
    [string]$Region = "ap-south-1"
)

$ErrorActionPreference = "Stop"

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host "   STREAMFORGE AWS INFRASTRUCTURE DEPLOYMENT" -ForegroundColor Cyan
Write-Host "============================================================`n" -ForegroundColor Cyan

# 1. Check AWS Credentials & Caller Identity
Write-Host "[1/5] Verifying AWS credentials and target region..." -ForegroundColor Yellow
try {
    $identityJson = aws sts get-caller-identity --output json | ConvertFrom-Json
    Write-Host "  Account ID : $($identityJson.Account)" -ForegroundColor Green
    Write-Host "  IAM User   : $($identityJson.Arn)" -ForegroundColor Green
    Write-Host "  Region     : $Region" -ForegroundColor Green
} catch {
    Write-Host "  ERROR: Failed to connect to AWS. Please verify credentials." -ForegroundColor Red
    exit 1
}

# 2. Validate Template
Write-Host "`n[2/5] Validating CloudFormation template..." -ForegroundColor Yellow
$templatePath = Join-Path $PSScriptRoot "infra\streamforge-cfn.yml"
if (-not (Test-Path $templatePath)) {
    Write-Host "  ERROR: Template not found at $templatePath" -ForegroundColor Red
    exit 1
}

try {
    aws cloudformation validate-template --template-body "file://$templatePath" --region $Region | Out-Null
    Write-Host "  CloudFormation template syntax valid!" -ForegroundColor Green
} catch {
    Write-Host "  ERROR: CloudFormation template validation failed: $_" -ForegroundColor Red
    exit 1
}

# 3. Check if Stack already exists
Write-Host "`n[3/5] Checking stack status for '$StackName'..." -ForegroundColor Yellow
$stackExists = $false
try {
    $desc = aws cloudformation describe-stacks --stack-name $StackName --region $Region 2>$null | ConvertFrom-Json
    if ($desc.Stacks.Count -gt 0) {
        $stackExists = $true
        $currentStatus = $desc.Stacks[0].StackStatus
        Write-Host "  Stack already exists with status: $currentStatus" -ForegroundColor Cyan
    }
} catch {
    $stackExists = $false
}

# 4. Initiate Stack Creation or Update
if (-not $stackExists) {
    Write-Host "`n[4/5] Initiating creation of '$StackName'..." -ForegroundColor Yellow
    aws cloudformation create-stack `
        --stack-name $StackName `
        --template-body "file://$templatePath" `
        --capabilities CAPABILITY_NAMED_IAM `
        --region $Region | Out-Null
    Write-Host "  Stack creation initiated. Monitoring live terminal progress..." -ForegroundColor Green
} else {
    Write-Host "`n[4/5] Initiating update of '$StackName'..." -ForegroundColor Yellow
    try {
        aws cloudformation update-stack `
            --stack-name $StackName `
            --template-body "file://$templatePath" `
            --capabilities CAPABILITY_NAMED_IAM `
            --region $Region | Out-Null
        Write-Host "  Stack update initiated. Monitoring live terminal progress..." -ForegroundColor Green
    } catch {
        if ($_ -like "*No updates are to be performed*") {
            Write-Host "  Stack is already up-to-date with current template." -ForegroundColor Green
        } else {
            Write-Host "  ERROR updating stack: $_" -ForegroundColor Red
            exit 1
        }
    }
}

# 5. Live Event Progress Streamer
Write-Host "`n================ LIVE AWS DEPLOYMENT PROGRESS ================" -ForegroundColor Cyan
$seenEvents = @{}
$isFinished = $false
$finalStatus = ""

while (-not $isFinished) {
    Start-Sleep -Seconds 5
    try {
        $eventsJson = aws cloudformation describe-stack-events --stack-name $StackName --region $Region --output json 2>$null | ConvertFrom-Json
        if ($eventsJson -and $eventsJson.StackEvents) {
            # Sort events chronologically
            $events = $eventsJson.StackEvents | Sort-Object Timestamp

            foreach ($event in $events) {
                if (-not $seenEvents.ContainsKey($event.EventId)) {
                    $seenEvents[$event.EventId] = $true

                    $time = ([DateTime]$event.Timestamp).ToString("HH:mm:ss")
                    $resType = $event.ResourceType.PadRight(35)
                    $resId = $event.LogicalResourceId.PadRight(30)
                    $status = $event.ResourceStatus

                    $color = "White"
                    if ($status -like "*COMPLETE*") { $color = "Green" }
                    elseif ($status -like "*IN_PROGRESS*") { $color = "Yellow" }
                    elseif ($status -like "*FAILED*") { $color = "Red" }

                    Write-Host "[$time] " -NoNewline -ForegroundColor Gray
                    Write-Host "$resType " -NoNewline -ForegroundColor DarkCyan
                    Write-Host "$resId " -NoNewline -ForegroundColor White
                    Write-Host "$status" -ForegroundColor $color

                    if ($event.ResourceStatusReason) {
                        Write-Host "         Reason: $($event.ResourceStatusReason)" -ForegroundColor DarkGray
                    }

                    # Check if the stack itself has reached a final state
                    if ($event.LogicalResourceId -eq $StackName) {
                        if ($status -in @("CREATE_COMPLETE", "UPDATE_COMPLETE", "CREATE_FAILED", "ROLLBACK_COMPLETE", "UPDATE_ROLLBACK_COMPLETE")) {
                            $isFinished = $true
                            $finalStatus = $status
                        }
                    }
                }
            }
        }
    } catch {
        # Ignore transient polling network errors
    }
}

Write-Host "==============================================================`n" -ForegroundColor Cyan

if ($finalStatus -in @("CREATE_COMPLETE", "UPDATE_COMPLETE")) {
    Write-Host " Stack deployed successfully!" -ForegroundColor Green
    Write-Host "`nRetrieving deployed service endpoints..." -ForegroundColor Yellow
    
    $outputs = aws cloudformation describe-stacks --stack-name $StackName --region $Region --query "Stacks[0].Outputs" --output json | ConvertFrom-Json
    
    Write-Host "`n---------------- DEPLOYED SERVICE ENDPOINTS ----------------" -ForegroundColor Cyan
    foreach ($out in $outputs) {
        Write-Host "$($out.OutputKey.PadRight(28)) : $($out.OutputValue)" -ForegroundColor Green
        if ($out.Description) {
            Write-Host "  ($($out.Description))" -ForegroundColor Gray
        }
    }
    Write-Host "------------------------------------------------------------`n" -ForegroundColor Cyan
} else {
    Write-Host " Stack deployment finished with status: $finalStatus" -ForegroundColor Red
    exit 1
}
