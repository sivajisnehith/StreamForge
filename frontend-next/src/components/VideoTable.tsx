'use client';

import React, { useState } from 'react';
import { Play, Trash2, Video, CheckCircle2, RotateCw, AlertTriangle, Clock, Search } from 'lucide-react';

interface VideoResponse {
  videoId: string;
  title: string;
  originalFileName: string;
  fileSize: number;
  status: 'UPLOADED' | 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  uploadedAt: string;
  width?: number;
  height?: number;
  videoCodec?: string;
  thumbnailUrl?: string;
  masterPlaylistUrl?: string;
}

interface VideoTableProps {
  videos: VideoResponse[];
  onRefresh?: () => void;
  onStream?: (streamUrl: string, title: string) => void;
  searchQuery?: string;
  onSearchChange?: (query: string) => void;
}

function formatBytes(bytes: number) {
  if (!bytes) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
}

export default function VideoTable({ videos = [], onRefresh, onStream, searchQuery, onSearchChange }: VideoTableProps) {
  const [localSearch, setLocalSearch] = useState(searchQuery || '');
  const [activeVideoId, setActiveVideoId] = useState<string | null>(null);

  // Keep local search in sync with prop
  React.useEffect(() => {
    setLocalSearch(searchQuery || '');
  }, [searchQuery]);

  // Debounce calling parent onSearchChange
  React.useEffect(() => {
    const delayDebounce = setTimeout(() => {
      if (onSearchChange && localSearch !== searchQuery) {
        onSearchChange(localSearch);
      }
    }, 300);
    return () => clearTimeout(delayDebounce);
  }, [localSearch, onSearchChange, searchQuery]);

  const handleDelete = async (videoId: string) => {
    if (!confirm('Are you sure you want to delete this video job?')) return;

    try {
      const storedToken = localStorage.getItem('token');
      if (!storedToken) return;

      const response = await fetch(`http://localhost:8080/api/videos/${videoId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${storedToken}`
        }
      });

      if (response.ok) {
        if (onRefresh) onRefresh();
      } else {
        alert('Failed to delete video job.');
      }
    } catch (e) {
      console.error('Error deleting video job:', e);
    }
  };

  const getStatusBadge = (status: VideoResponse['status']) => {
    switch (status) {
      case 'COMPLETED':
        return (
          <span className="inline-flex items-center gap-1 rounded-full border border-green-500/10 bg-green-500/5 px-2.5 py-0.5 font-mono text-[9px] font-bold uppercase text-[#2ecc71] shadow-[0_0_8px_rgba(46,204,113,0.1)]">
            <CheckCircle2 className="h-3 w-3" />
            Ready
          </span>
        );
      case 'PROCESSING':
        return (
          <span className="inline-flex items-center gap-1 rounded-full border border-[#ff6a28]/10 bg-[#ff6a28]/5 px-2.5 py-0.5 font-mono text-[9px] font-bold uppercase text-[#ff6a28] shadow-[0_0_8px_rgba(255,106,40,0.1)]">
            <RotateCw className="h-3 w-3 animate-spin" />
            Processing
          </span>
        );
      case 'PENDING':
        return (
          <span className="inline-flex items-center gap-1 rounded-full border border-[#34d1c4]/10 bg-[#34d1c4]/5 px-2.5 py-0.5 font-mono text-[9px] font-bold uppercase text-[#34d1c4] shadow-[0_0_8px_rgba(52,209,196,0.1)]">
            <Clock className="h-3 w-3" />
            Queued
          </span>
        );
      case 'FAILED':
        return (
          <span className="inline-flex items-center gap-1 rounded-full border border-red-500/10 bg-red-500/5 px-2.5 py-0.5 font-mono text-[9px] font-bold uppercase text-[#ff5f56] shadow-[0_0_8px_rgba(255,95,86,0.1)]">
            <AlertTriangle className="h-3 w-3" />
            Failed
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center gap-1 rounded-full border border-white/5 bg-white/2 px-2.5 py-0.5 font-mono text-[9px] font-bold uppercase text-[#a2aebf]">
            Uploading
          </span>
        );
    }
  };

  const getProgress = (status: VideoResponse['status']) => {
    if (status === 'COMPLETED') return 100;
    if (status === 'PROCESSING') return 65;
    if (status === 'FAILED') return 15;
    return 0;
  };

  const formatUploadedDate = (dateStr: string) => {
    try {
      const d = new Date(dateStr);
      return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
    } catch {
      return dateStr;
    }
  };

  const handleStreamClick = (job: VideoResponse) => {
    if (job.masterPlaylistUrl && onStream) {
      onStream(`http://localhost:8080${job.masterPlaylistUrl}`, job.title || 'Untitled Ingest');
    }
  };

  return (
    <div className="glass-panel rounded-xl overflow-hidden shadow-2xl">
      <div className="p-6 border-b border-white/10 bg-[#0a1020]/30 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex flex-col">
          <h2 className="font-sans text-lg font-bold tracking-tight text-white">
            Ingestion Logs Database
          </h2>
          <p className="text-xs text-[#a2aebf] mt-1">
            Monitor transcode stages, codecs, and stream master playlist endpoints.
          </p>
        </div>
        <div className="relative w-full sm:max-w-xs">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-3.5 w-3.5 text-[#ff6a28] drop-shadow-[0_0_4px_rgba(255,106,40,0.3)]" />
          <input
            type="text"
            placeholder="Search video titles..."
            value={localSearch}
            onChange={(e) => setLocalSearch(e.target.value)}
            className="w-full bg-[#050816]/90 border border-white/15 rounded-md pl-9 pr-4 py-1.5 font-mono text-xs text-white placeholder-zinc-500 hover:border-white/25 focus:outline-none focus:border-[#ff6a28] focus:shadow-[0_0_10px_rgba(255,106,40,0.2)] transition-all"
          />
        </div>
      </div>

      <div className="w-full overflow-x-auto">
        {videos.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 text-center">
            <div className="flex h-12 w-12 items-center justify-center rounded-full bg-white/5 text-[#636e7f] border border-white/5 mb-4">
              <Video className="h-5 w-5" />
            </div>
            <span className="text-sm font-semibold text-white">No Ingestion Jobs Found</span>
            <span className="text-xs text-[#636e7f] mt-1">Ingest a new MP4/MKV video to start processing</span>
          </div>
        ) : (
          <table className="w-full border-collapse text-left text-xs">
            <thead>
              <tr className="border-b border-white/5 bg-white/[0.01]">
                <th className="font-mono text-[9px] font-bold uppercase tracking-wider text-[#636e7f] p-4">Title / Source</th>
                <th className="font-mono text-[9px] font-bold uppercase tracking-wider text-[#636e7f] p-4">Resolution</th>
                <th className="font-mono text-[9px] font-bold uppercase tracking-wider text-[#636e7f] p-4">Codec</th>
                <th className="font-mono text-[9px] font-bold uppercase tracking-wider text-[#636e7f] p-4">Status</th>
                <th className="font-mono text-[9px] font-bold uppercase tracking-wider text-[#636e7f] p-4">Progress</th>
                <th className="font-mono text-[9px] font-bold uppercase tracking-wider text-[#636e7f] p-4">Uploaded</th>
                <th className="font-mono text-[9px] font-bold uppercase tracking-wider text-[#636e7f] p-4">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5">
              {videos.map((job) => (
                <tr key={job.videoId} className="hover:bg-white/[0.015] transition-colors group">
                  {/* Thumbnail & Title info */}
                  <td className="p-4">
                    <div className="flex items-center gap-4">
                      <div className="flex h-10 w-14 shrink-0 items-center justify-center rounded bg-[#050816] border border-white/5 text-[#636e7f] overflow-hidden group-hover:border-white/10 transition-all relative">
                        {job.thumbnailUrl ? (
                          // eslint-disable-next-line @next/next/no-img-element
                          <img
                            src={`http://localhost:8080${job.thumbnailUrl}`}
                            alt="Transcode Thumbnail"
                            className="w-full h-full object-cover group-hover:scale-105 transition-all duration-300"
                          />
                        ) : (
                          <Video className="h-4.5 w-4.5 text-[#ff6a28] opacity-80" />
                        )}
                      </div>
                      <div className="flex flex-col min-w-0">
                        <span className="font-sans text-xs font-semibold text-white truncate max-w-[240px]">
                          {job.title || 'Untitled Ingest'}
                        </span>
                        <span className="font-mono text-[9px] text-[#636e7f] truncate max-w-[240px] mt-0.5">
                          {job.originalFileName} · {formatBytes(job.fileSize)}
                        </span>
                      </div>
                    </div>
                  </td>
                  
                  {/* Resolution details */}
                  <td className="font-mono text-[#a2aebf] p-4">
                    {job.width && job.height ? (
                      <span className="flex items-center gap-1.5">
                        {job.width}x{job.height}
                        <span className="rounded bg-white/5 px-1 py-0.5 text-[8px] font-bold text-white uppercase">
                          {job.height >= 2160 ? '4K' : job.height >= 1080 ? 'FHD' : 'HD'}
                        </span>
                      </span>
                    ) : (
                      <span className="text-[#636e7f]">—</span>
                    )}
                  </td>
                  
                  {/* Codec info */}
                  <td className="font-mono text-[#a2aebf] p-4 uppercase">
                    {job.videoCodec || <span className="text-[#636e7f]">—</span>}
                  </td>
                  
                  {/* Status Badge */}
                  <td className="p-4">{getStatusBadge(job.status)}</td>
                  
                  {/* Progress bar */}
                  <td className="p-4">
                    <div className="flex flex-col gap-1.5 w-28">
                      <span className="font-mono text-[9px] font-semibold text-white">
                        {getProgress(job.status)}%
                      </span>
                      <div className="h-1 w-full bg-white/5 rounded-full overflow-hidden border border-white/5">
                        <div 
                          className={`h-full rounded-full transition-all duration-500 ${
                            job.status === 'FAILED' 
                              ? 'bg-[#ff5f56]' 
                              : job.status === 'COMPLETED' 
                              ? 'bg-gradient-to-r from-green-500 to-emerald-400' 
                              : 'bg-gradient-to-r from-[#ff6a28] to-[#9333ea]'
                          }`} 
                          style={{ width: `${getProgress(job.status)}%` }} 
                        />
                      </div>
                    </div>
                  </td>
                  
                  {/* Upload Date */}
                  <td className="font-mono text-[#636e7f] p-4">
                    {formatUploadedDate(job.uploadedAt)}
                  </td>
                  
                  {/* Actions */}
                  <td className="p-4">
                    <div className="flex items-center gap-4">
                      <button
                        onClick={() => handleStreamClick(job)}
                        disabled={job.status !== 'COMPLETED'}
                        className="font-mono text-[10px] font-bold text-[#34d1c4] disabled:opacity-40 disabled:cursor-not-allowed hover:text-[#4dfdf0] transition-colors flex items-center gap-1.5 cursor-pointer"
                      >
                        <Play className="h-3 w-3" />
                        Stream
                      </button>
                      <button 
                        onClick={() => handleDelete(job.videoId)}
                        className="text-[#636e7f] hover:text-[#ff5f56] transition-colors cursor-pointer"
                      >
                        <Trash2 className="h-3.5 w-3.5" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
