'use client';

import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Upload, Database, Layers, Cpu, Server, Play } from 'lucide-react';

interface NodeInfo {
  label: string;
  sub: string;
  icon: React.ReactNode;
  color: string;
  glow: string;
}

interface ProcessingPipelineProps {
  videos: any[];
}

export default function ProcessingPipeline({ videos = [] }: ProcessingPipelineProps) {
  const [activeStep, setActiveStep] = useState(5);
  const [isFailed, setIsFailed] = useState(false);
  const [activeVideoTitle, setActiveVideoTitle] = useState<string | null>(null);

  useEffect(() => {
    if (videos.length === 0) {
      setActiveStep(5);
      setIsFailed(false);
      setActiveVideoTitle(null);
      return;
    }

    // Grab the latest uploaded video
    const latestVideo = videos[0];
    const status = latestVideo.status;
    setActiveVideoTitle(latestVideo.title);

    setIsFailed(status === 'FAILED');

    if (status === 'PROCESSING') {
      // Cycle steps 2 (Worker Pool), 3 (FFmpeg Core), and 4 (MinIO Storage) to visualize active flow
      let cycle = 2;
      setActiveStep(cycle);

      const interval = setInterval(() => {
        cycle = cycle === 4 ? 2 : cycle + 1;
        setActiveStep(cycle);
      }, 1300);

      return () => clearInterval(interval);
    } else {
      switch (status) {
        case 'UPLOADED':
          setActiveStep(0);
          break;
        case 'PENDING':
          setActiveStep(1);
          break;
        case 'COMPLETED':
          setActiveStep(5);
          break;
        case 'FAILED':
          setActiveStep(3); // stopped at transcoding
          break;
        default:
          setActiveStep(5);
      }
    }
  }, [videos]);

  const pipelineNodes: NodeInfo[] = [
    { label: 'Intake Ingest', sub: 'Upload Client', icon: <Upload className="h-4.5 w-4.5" />, color: '#ff6a28', glow: 'shadow-[0_0_15px_rgba(255,106,40,0.2)]' },
    { label: 'RabbitMQ', sub: 'Task Queue', icon: <Database className="h-4.5 w-4.5" />, color: '#34d1c4', glow: 'shadow-[0_0_15px_rgba(52,209,196,0.2)]' },
    { label: 'Worker Pool', sub: 'Node Scheduler', icon: <Layers className="h-4.5 w-4.5" />, color: '#9333ea', glow: 'shadow-[0_0_15px_rgba(147,51,234,0.2)]' },
    { label: 'FFmpeg Core', sub: isFailed ? 'Transcode Fail' : 'Segment Transcode', icon: <Cpu className="h-4.5 w-4.5" />, color: isFailed ? '#ff5f56' : '#ff6a28', glow: isFailed ? 'shadow-[0_0_15px_rgba(255,95,86,0.25)]' : 'shadow-[0_0_15px_rgba(255,106,40,0.2)]' },
    { label: 'MinIO Storage', sub: 'Object Bucket', icon: <Server className="h-4.5 w-4.5" />, color: '#34d1c4', glow: 'shadow-[0_0_15px_rgba(52,209,196,0.2)]' },
    { label: 'HLS Delivery', sub: 'Adaptive Stream', icon: <Play className="h-4.5 w-4.5" />, color: '#2ecc71', glow: 'shadow-[0_0_15px_rgba(46,204,113,0.2)]' },
  ];

  return (
    <div className="glass-panel overflow-hidden rounded-xl p-8">
      {/* Top Header */}
      <div className="flex flex-col gap-2 mb-10">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="font-mono text-[9px] text-[#ff6a28] tracking-widest uppercase">system_architecture</span>
            <span className="h-[1px] w-6 bg-[#ff6a28]/20" />
          </div>
          {activeVideoTitle && (
            <span className="font-mono text-[9px] text-[#636e7f] uppercase truncate max-w-[200px]">
              Tracking: {activeVideoTitle}
            </span>
          )}
        </div>
        <h2 className="font-sans text-lg font-bold tracking-tight text-white">
          Live Ingestion Pipeline
        </h2>
        <p className="text-xs text-[#a2aebf]">
          Real-time tracking of asynchronous video processing and chunked segments distribution.
        </p>
      </div>

      {/* Pipeline Visual Container */}
      <div className="relative flex flex-col md:flex-row items-center justify-between gap-8 md:gap-4 py-8">
        
        {/* SVG Connector paths */}
        <div className="absolute inset-0 hidden md:flex items-center justify-between px-12 pointer-events-none -z-10">
          <svg className="w-full h-4 text-white/5" viewBox="0 0 100 4" preserveAspectRatio="none">
            <defs>
              <linearGradient id="pathGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop offset="0%" stopColor="#ff6a28" stopOpacity="0.2" />
                <stop offset="50%" stopColor="#34d1c4" stopOpacity="0.2" />
                <stop offset="100%" stopColor="#2ecc71" stopOpacity="0.2" />
              </linearGradient>
            </defs>
            <line x1="0" y1="2" x2="100" y2="2" stroke="url(#pathGradient)" strokeWidth="1.5" />
          </svg>
        </div>

        {/* Floating animated data packets */}
        <div className="absolute inset-0 hidden md:flex items-center justify-between px-12 pointer-events-none -z-10">
          <div className="relative w-full h-full flex items-center">
            <motion.div
              animate={{ left: ['0%', '100%'] }}
              transition={{ repeat: Infinity, duration: 8, ease: 'linear' }}
              className="absolute h-2 w-2 rounded-full bg-[#ff6a28] shadow-[0_0_12px_#ff6a28] border border-white/20"
            />
            <motion.div
              animate={{ left: ['0%', '100%'] }}
              transition={{ repeat: Infinity, duration: 8, delay: 2.5, ease: 'linear' }}
              className="absolute h-2 w-2 rounded-full bg-[#34d1c4] shadow-[0_0_12px_#34d1c4] border border-white/20"
            />
            <motion.div
              animate={{ left: ['0%', '100%'] }}
              transition={{ repeat: Infinity, duration: 8, delay: 5, ease: 'linear' }}
              className="absolute h-2 w-2 rounded-full bg-[#2ecc71] shadow-[0_0_12px_#2ecc71] border border-white/20"
            />
          </div>
        </div>

        {/* Nodes */}
        {pipelineNodes.map((node, i) => {
          const isActive = i === activeStep;
          const isDone = i < activeStep;
          const color = node.color;

          return (
            <div key={node.label} className="relative flex flex-col items-center gap-3.5 z-10 flex-1">
              
              {/* Outer pulsing ring */}
              <div className="relative flex items-center justify-center">
                <motion.div
                  animate={isActive ? { scale: [1, 1.15, 1], opacity: [0.15, 0.4, 0.15] } : { scale: 1, opacity: 0 }}
                  transition={{ repeat: Infinity, duration: 2, ease: 'easeInOut' }}
                  className="absolute h-20 w-20 rounded-full"
                  style={{ backgroundColor: color }}
                />

                <motion.div
                  animate={isActive ? { scale: [1, 1.25, 1], opacity: [0.08, 0.2, 0.08] } : { scale: 1, opacity: 0 }}
                  transition={{ repeat: Infinity, duration: 2, delay: 0.4, ease: 'easeInOut' }}
                  className="absolute h-24 w-24 rounded-full"
                  style={{ backgroundColor: color }}
                />

                {/* Main Node bubble */}
                <div
                  className={`flex h-14 w-14 items-center justify-center rounded-full border-2 bg-[#050816] shadow-2xl transition-all duration-500 ${
                    isActive 
                      ? 'scale-110 border-current shadow-lg' 
                      : isDone 
                      ? 'border-white/20 text-[#a2aebf]' 
                      : 'border-white/5 text-[#636e7f]'
                  }`}
                  style={{ 
                    color: isActive ? color : 'inherit',
                    boxShadow: isActive ? `0 0 20px -5px ${color}` : 'none'
                  }}
                >
                  {node.icon}
                </div>
              </div>

              {/* Text labels */}
              <div className="flex flex-col items-center text-center">
                <span className={`font-sans text-xs font-semibold ${isActive ? 'text-white' : 'text-[#a2aebf]'}`}>
                  {node.label}
                </span>
                <span className="font-mono text-[9px] text-[#636e7f] uppercase mt-0.5 tracking-wider">
                  {node.sub}
                </span>
              </div>
            </div>
          );
        })}

      </div>
    </div>
  );
}
