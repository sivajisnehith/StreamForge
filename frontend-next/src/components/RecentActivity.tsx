'use client';

import React from 'react';
import { Upload, Cpu, Play, Database, Zap, AlertTriangle } from 'lucide-react';

interface VideoResponse {
  videoId: string;
  title: string;
  originalFileName: string;
  status: 'UPLOADED' | 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  uploadedAt: string;
}

interface RecentActivityProps {
  videos?: VideoResponse[];
}

interface Activity {
  id: string;
  icon: React.ReactNode;
  title: string;
  description: string;
  time: string;
  color: string;
  glow: string;
}

export default function RecentActivity({ videos = [] }: RecentActivityProps) {
  
  const getActivityLog = (job: VideoResponse): Activity => {
    const title = job.title || 'Untitled Ingest';
    const filename = job.originalFileName;

    const timeString = (() => {
      try {
        const diffMs = new Date().getTime() - new Date(job.uploadedAt).getTime();
        const diffMins = Math.floor(diffMs / 60000);
        if (diffMins < 1) return 'Just now';
        if (diffMins < 60) return `${diffMins}m ago`;
        const diffHours = Math.floor(diffMins / 60);
        if (diffHours < 24) return `${diffHours}h ago`;
        return new Date(job.uploadedAt).toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
      } catch {
        return 'Recently';
      }
    })();

    switch (job.status) {
      case 'COMPLETED':
        return {
          id: job.videoId,
          icon: <Play className="h-3.5 w-3.5" />,
          title: 'HLS Stream Published',
          description: `Playlist index and chunks generated for ${title}.`,
          time: timeString,
          color: '#2ecc71',
          glow: 'rgba(46,204,113,0.25)',
        };
      case 'PROCESSING':
        return {
          id: job.videoId,
          icon: <Cpu className="h-3.5 w-3.5" />,
          title: 'FFmpeg Core Active',
          description: `Worker segmenting loops executing on ${filename}.`,
          time: timeString,
          color: '#ff6a28',
          glow: 'rgba(255,106,40,0.25)',
        };
      case 'PENDING':
        return {
          id: job.videoId,
          icon: <Database className="h-3.5 w-3.5" />,
          title: 'RabbitMQ Task Queued',
          description: `Ingestion scheduled. Awaiting available FFmpeg slot.`,
          time: timeString,
          color: '#34d1c4',
          glow: 'rgba(52,209,196,0.25)',
        };
      case 'FAILED':
        return {
          id: job.videoId,
          icon: <AlertTriangle className="h-3.5 w-3.5" />,
          title: 'Transcoding Failed',
          description: `FFmpeg process exited abnormally on ${filename}.`,
          time: timeString,
          color: '#ff5f56',
          glow: 'rgba(255,95,86,0.25)',
        };
      default:
        return {
          id: job.videoId,
          icon: <Upload className="h-3.5 w-3.5" />,
          title: 'Raw Payload Received',
          description: `${filename} written successfully to raw storage.`,
          time: timeString,
          color: '#34d1c4',
          glow: 'rgba(52,209,196,0.25)',
        };
    }
  };

  // Convert latest 5 video tasks into activity logs
  const activities = videos.slice(0, 5).map(v => getActivityLog(v));

  return (
    <div className="glass-panel rounded-xl p-6 flex flex-col justify-between h-full min-h-[350px]">
      <div className="flex flex-col gap-1 mb-8">
        <h3 className="font-sans text-sm font-bold text-white">System Activity</h3>
        <span className="font-mono text-[9px] text-[#636e7f] uppercase">Real-time operation audit logs</span>
      </div>

      <div className="relative pl-6 flex flex-col gap-7 flex-grow">
        {activities.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 text-center my-auto">
            <span className="text-xs text-[#636e7f] font-mono uppercase">Timeline logs await ingestion</span>
          </div>
        ) : (
          <>
            {/* Timeline connector dashed bar */}
            <div className="absolute left-[11px] top-2.5 bottom-2.5 w-[1px] border-l border-dashed border-white/10" />

            {activities.map((act, i) => {
              const isLatest = i === 0;

              return (
                <div key={act.id} className="relative flex flex-col gap-1">
                  
                  {/* Glowing concentric checkpoint */}
                  <div 
                    className="absolute -left-[22px] top-1 flex h-6 w-6 items-center justify-center rounded-full border bg-[#050816] shadow-md transition-all"
                    style={{ 
                      borderColor: isLatest ? act.color : 'rgba(255,255,255,0.06)',
                      color: act.color,
                      boxShadow: isLatest ? `0 0 10px ${act.glow}` : 'none'
                    }}
                  >
                    {act.icon}
                  </div>

                  {/* Title & Time */}
                  <div className="flex justify-between items-baseline gap-4 ml-2">
                    <span className={`text-xs font-semibold ${isLatest ? 'text-white' : 'text-[#a2aebf]'}`}>
                      {act.title}
                    </span>
                    <span className="font-mono text-[9px] text-[#636e7f]">{act.time}</span>
                  </div>
                  
                  {/* Description */}
                  <p className="text-[11px] text-[#636e7f] leading-relaxed ml-2 max-w-[280px]">
                    {act.description}
                  </p>
                </div>
              );
            })}
          </>
        )}
      </div>
    </div>
  );
}
