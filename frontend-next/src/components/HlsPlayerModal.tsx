'use client';

import React, { useEffect, useRef, useState } from 'react';
import Hls from 'hls.js';
import { X, Video, ShieldAlert, Sliders, Terminal, Activity } from 'lucide-react';

interface HlsPlayerModalProps {
  streamUrl: string;
  title: string;
  onClose: () => void;
}

export default function HlsPlayerModal({ streamUrl, title, onClose }: HlsPlayerModalProps) {
  const videoRef = useRef<HTMLVideoElement>(null);
  const hlsRef = useRef<Hls | null>(null);
  const consoleEndRef = useRef<HTMLDivElement>(null);

  const [logs, setLogs] = useState<string[]>([]);
  const [availableLevels, setAvailableLevels] = useState<any[]>([]);
  const [currentLevelIdx, setCurrentLevelIdx] = useState<number>(-1); // -1 = Auto
  const [actualQuality, setActualQuality] = useState<string>('Auto (detecting...)');
  const [bandwidthCap, setBandwidthCap] = useState<number>(0); // 0 = Unlimited (in Kbps)
  const [bufferLength, setBufferLength] = useState<number>(0);
  const [currentBitrate, setCurrentBitrate] = useState<string>('N/A');

  const addLog = (msg: string) => {
    const timestamp = new Date().toLocaleTimeString(undefined, {
      hour12: false,
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
    setLogs((prev) => [...prev.slice(-49), `[${timestamp}] ${msg}`]);
  };

  // Auto-scroll the terminal logs
  useEffect(() => {
    consoleEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [logs]);

  // Monitor buffer length in video player
  useEffect(() => {
    const interval = setInterval(() => {
      if (videoRef.current) {
        const video = videoRef.current;
        let length = 0;
        for (let i = 0; i < video.buffered.length; i++) {
          const start = video.buffered.start(i);
          const end = video.buffered.end(i);
          if (video.currentTime >= start && video.currentTime <= end) {
            length = end - video.currentTime;
            break;
          }
        }
        setBufferLength(parseFloat(length.toFixed(1)));
      }
    }, 800);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    if (!videoRef.current || !streamUrl) return;

    const video = videoRef.current;
    addLog(`Initializing HLS.js player for stream: ${streamUrl.substring(streamUrl.lastIndexOf('/'))}`);

    if (hlsRef.current) {
      hlsRef.current.destroy();
    }

    if (Hls.isSupported()) {
      const hls = new Hls({
        enableWorker: true,
        lowLatencyMode: true,
        backBufferLength: 30
      });
      hlsRef.current = hls;

      addLog('HLS.js player instance instantiated.');

      hls.loadSource(streamUrl);
      hls.attachMedia(video);

      // --- Bind HLS.js Event Hooks for live terminal logs ---
      hls.on(Hls.Events.MEDIA_ATTACHED, () => {
        addLog('Media element attached to player core.');
      });

      hls.on(Hls.Events.MANIFEST_LOADED, (event, data) => {
        addLog(`Manifest loaded successfully. Server responded with HTTP 200.`);
      });

      hls.on(Hls.Events.MANIFEST_PARSED, (event, data) => {
        setAvailableLevels(data.levels);
        const resList = data.levels.map(l => `${l.height}p`).join(', ');
        addLog(`Parser found ${data.levels.length} HLS renditions: [${resList}]`);
        video.play().catch(() => addLog('Autoplay prevented. User interaction required.'));
      });

      hls.on(Hls.Events.LEVEL_SWITCHED, (event, data) => {
        setCurrentLevelIdx(hls.currentLevel);
        const level = hls.levels[data.level];
        if (level) {
          setActualQuality(`${level.height}p`);
          setCurrentBitrate(`${(level.bitrate / 1000000).toFixed(2)} Mbps`);
          addLog(`ABR Controller switched active stream resolution to ${level.height}p`);
        }
      });

      hls.on(Hls.Events.FRAG_LOADING, (event, data) => {
        addLog(`Fetching media chunk ${data.frag.sn} (quality level: ${data.frag.level})`);
      });

      hls.on(Hls.Events.FRAG_LOADED, (event, data: any) => {
        const loadTime = data.stats ? `${data.stats.tload}ms` : 'N/A';
        addLog(`Chunk ${data.frag.sn} downloaded. Size: ${(data.payload.byteLength / 1024).toFixed(0)} KB in ${loadTime}.`);
      });

      hls.on(Hls.Events.BUFFER_APPENDED, () => {
        // Appended buffer
      });

      hls.on(Hls.Events.ERROR, (event, data) => {
        addLog(`Engine Warning: ${data.details} (${data.type})`);
        if (data.fatal) {
          switch (data.type) {
            case Hls.ErrorTypes.NETWORK_ERROR:
              addLog('Fatal network error. Retrying fragment downloads...');
              hls.startLoad();
              break;
            case Hls.ErrorTypes.MEDIA_ERROR:
              addLog('Fatal media playback error. Attempting buffer recovery...');
              hls.recoverMediaError();
              break;
            default:
              addLog('Fatal unrecoverable HLS error. Destroying engine context.');
              break;
          }
        }
      });

      return () => {
        hls.destroy();
        hlsRef.current = null;
      };
    } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
      // Fallback for native Safari HLS
      video.src = streamUrl;
      addLog('Initialized native Safari HLS player.');
      video.addEventListener('loadedmetadata', () => {
        video.play().catch(() => {});
      });
    }
  }, [streamUrl]);

  // Adjust bandwidth cap dynamically
  useEffect(() => {
    const hls = hlsRef.current;
    if (!hls || availableLevels.length === 0) return;

    if (bandwidthCap === 0) {
      // Unlimited
      hls.autoLevelCapping = -1;
      addLog(`Bandwidth Limit removed. Adaptive ABR controller unlocked.`);
    } else {
      // Find levels that fit under the bandwidth cap (convert Kbps cap to bps)
      const capBps = bandwidthCap * 1000;
      let targetMaxLevel = 0;

      // Find the highest level that has a bitrate <= capBps
      for (let i = 0; i < availableLevels.length; i++) {
        if (availableLevels[i].bitrate <= capBps) {
          targetMaxLevel = i;
        }
      }

      hls.autoLevelCapping = targetMaxLevel;
      const targetLevel = availableLevels[targetMaxLevel];
      addLog(
        `Bandwidth Cap throttled to ${bandwidthCap} Kbps. Restricting max ABR level to ${targetLevel.height}p (requires ${(targetLevel.bitrate / 1000).toFixed(0)} Kbps).`
      );
    }
  }, [bandwidthCap, availableLevels]);

  // Forcing specific quality levels
  const handleQualityChange = (levelIdx: number) => {
    const hls = hlsRef.current;
    if (!hls) return;

    hls.currentLevel = levelIdx;
    setCurrentLevelIdx(levelIdx);

    if (levelIdx === -1) {
      addLog('Forced stream quality: Auto ABR');
      setActualQuality('Auto (detecting...)');
    } else {
      const level = hls.levels[levelIdx];
      addLog(`Forcing stream quality level index: ${levelIdx} (${level.height}p)`);
      setActualQuality(`${level.height}p (Forced)`);
      setCurrentBitrate(`${(level.bitrate / 1000000).toFixed(2)} Mbps`);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/85 backdrop-blur-md p-4">
      <div className="glass-panel w-full max-w-5xl rounded-xl overflow-hidden shadow-2xl flex flex-col md:flex-row h-[90vh] max-h-[640px]">
        
        {/* Left Side: Video Player & Throttle Controls */}
        <div className="flex-1 flex flex-col justify-between bg-[#040609] p-4 border-r border-white/5">
          {/* Header */}
          <div className="flex items-center justify-between pb-3 border-b border-white/5 mb-4">
            <div className="flex items-center gap-2">
              <Video className="h-4.5 w-4.5 text-[#34d1c4]" />
              <span className="font-sans text-sm font-bold text-white truncate max-w-[280px]">
                {title}
              </span>
            </div>
            <div className="flex items-center gap-1.5 rounded bg-green-500/10 px-2 py-0.5 font-mono text-[9px] text-green-400">
              <span className="h-1.5 w-1.5 rounded-full bg-green-500 animate-ping" />
              Active Feed
            </div>
          </div>

          {/* Video element container */}
          <div className="relative aspect-video bg-black rounded-lg overflow-hidden border border-white/5 flex items-center justify-center">
            <video
              ref={videoRef}
              controls
              playsInline
              className="w-full h-full object-contain"
            />
          </div>

          {/* Live telemetry values */}
          <div className="grid grid-cols-3 gap-3 my-4">
            <div className="bg-white/2 rounded p-2.5 border border-white/5">
              <span className="block text-[8px] font-mono text-[#636e7f] uppercase">Selected Quality</span>
              <span className="text-xs font-bold text-white font-mono mt-0.5 block">{actualQuality}</span>
            </div>
            <div className="bg-white/2 rounded p-2.5 border border-white/5">
              <span className="block text-[8px] font-mono text-[#636e7f] uppercase">Current Bitrate</span>
              <span className="text-xs font-bold text-[#ff6a28] font-mono mt-0.5 block">{currentBitrate}</span>
            </div>
            <div className="bg-white/2 rounded p-2.5 border border-white/5">
              <span className="block text-[8px] font-mono text-[#636e7f] uppercase">Active Buffer</span>
              <span className="text-xs font-bold text-[#34d1c4] font-mono mt-0.5 block">{bufferLength}s cached</span>
            </div>
          </div>

          {/* Network Throttling & Quality Selectors */}
          <div className="space-y-4 pt-2 border-t border-white/5">
            {/* Bandwidth Cap Slider */}
            <div>
              <div className="flex justify-between items-center mb-1">
                <label className="flex items-center gap-1.5 font-mono text-[9px] uppercase text-[#636e7f]">
                  <Sliders className="h-3 w-3 text-[#ff6a28]" />
                  Simulate Network Bandwidth
                </label>
                <span className="font-mono text-xs font-bold text-white">
                  {bandwidthCap === 0 ? 'Unlimited (Auto)' : `${(bandwidthCap / 1000).toFixed(1)} Mbps`}
                </span>
              </div>
              <input
                type="range"
                min="0"
                max="12000"
                step="250"
                value={bandwidthCap}
                onChange={(e) => setBandwidthCap(parseInt(e.target.value))}
                className="w-full h-1 bg-white/10 rounded-lg appearance-none cursor-pointer accent-[#ff6a28]"
              />
              <div className="flex justify-between text-[8px] font-mono text-[#636e7f] mt-1">
                <span>Throttle Cap (250 Kbps)</span>
                <span>Max Ingest (12 Mbps)</span>
              </div>
            </div>

            {/* Quality override buttons */}
            <div>
              <span className="block font-mono text-[9px] uppercase text-[#636e7f] mb-1.5">Force Ingestion Quality</span>
              <div className="flex flex-wrap gap-2">
                <button
                  onClick={() => handleQualityChange(-1)}
                  className={`px-3 py-1 font-mono text-[10px] rounded border transition-all cursor-pointer ${
                    currentLevelIdx === -1
                      ? 'border-[#34d1c4] bg-[#34d1c4]/5 text-[#34d1c4] font-bold'
                      : 'border-white/5 bg-white/2 text-[#636e7f] hover:text-white hover:border-white/10'
                  }`}
                >
                  Auto ABR
                </button>
                {availableLevels.map((lvl, index) => (
                  <button
                    key={index}
                    onClick={() => handleQualityChange(index)}
                    className={`px-3 py-1 font-mono text-[10px] rounded border transition-all cursor-pointer ${
                      currentLevelIdx === index
                        ? 'border-[#ff6a28] bg-[#ff6a28]/5 text-[#ff6a28] font-bold'
                        : 'border-white/5 bg-white/2 text-[#636e7f] hover:text-white hover:border-white/10'
                    }`}
                  >
                    {lvl.height}p
                  </button>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* Right Side: Scrolling HLS Terminal Logs Console */}
        <div className="w-full md:w-[350px] bg-[#020407] flex flex-col justify-between">
          {/* Header */}
          <div className="flex items-center justify-between p-4 border-b border-white/5 bg-[#07090d]">
            <div className="flex items-center gap-2">
              <Terminal className="h-4 w-4 text-[#ff6a28]" />
              <span className="font-mono text-xs font-bold text-white">HLS Engine Console</span>
            </div>
            <button
              onClick={onClose}
              className="text-[#636e7f] hover:text-white p-1 rounded hover:bg-white/5 transition-all cursor-pointer"
            >
              <X className="h-5 w-5" />
            </button>
          </div>

          {/* Scrolling output viewport */}
          <div className="flex-grow p-4 overflow-y-auto font-mono text-[9.5px] leading-relaxed text-zinc-400 space-y-2 select-text selection:bg-[#ff6a28]/20 select-all">
            {logs.length === 0 ? (
              <div className="flex h-full items-center justify-center text-center text-[#636e7f] uppercase text-[9px] tracking-wider py-24">
                Awaiting player attachment...
              </div>
            ) : (
              logs.map((log, idx) => {
                let colorClass = 'text-[#a2aebf]';
                if (log.includes('Engine switched')) colorClass = 'text-[#2ecc71] font-semibold';
                if (log.includes('Bandwidth Cap') || log.includes('Forcing')) colorClass = 'text-[#ff6a28] font-semibold';
                if (log.includes('Engine Warning')) colorClass = 'text-yellow-400';
                if (log.includes('Fatal')) colorClass = 'text-[#ff5f56] font-bold';

                return (
                  <div key={idx} className={`${colorClass} border-b border-white/2 pb-1.5 break-all`}>
                    {log}
                  </div>
                );
              })
            )}
            <div ref={consoleEndRef} />
          </div>

          {/* Footer status summary */}
          <div className="p-4 border-t border-white/5 bg-[#07090d] flex items-center justify-between font-mono text-[9px] text-[#636e7f]">
            <div className="flex items-center gap-1.5">
              <Activity className="h-3 w-3 text-[#34d1c4]" />
              <span>Diagnostic Feed: Active</span>
            </div>
            <button
              onClick={() => setLogs([])}
              className="hover:text-white transition-colors cursor-pointer"
            >
              Clear Buffer
            </button>
          </div>
        </div>

      </div>
    </div>
  );
}
