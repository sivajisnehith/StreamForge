'use client';

import React, { useState, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Upload, X, Check, Loader2, ShieldAlert } from 'lucide-react';

interface UploadCardProps {
  onUploadSuccess?: () => void;
  existingCount: number;
}

export default function UploadCard({ onUploadSuccess, existingCount }: UploadCardProps) {
  const [dragActive, setDragActive] = useState(false);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [isUploading, setIsUploading] = useState(false);
  const [progress, setProgress] = useState(0);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const isLimitReached = existingCount >= 3;

  // SVG progress ring variables
  const radius = 32;
  const stroke = 3;
  const normalizedRadius = radius - stroke * 2;
  const circumference = normalizedRadius * 2 * Math.PI;
  const strokeDashoffset = circumference - (progress / 100) * circumference;

  const handleDrag = (e: React.DragEvent) => {
    if (isLimitReached) return;
    e.preventDefault();
    e.stopPropagation();
    if (e.type === "dragenter" || e.type === "dragover") {
      setDragActive(true);
    } else if (e.type === "dragleave") {
      setDragActive(false);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    if (isLimitReached) return;
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFile(e.dataTransfer.files[0]);
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      handleFile(e.target.files[0]);
    }
  };

  const handleFile = (file: File) => {
    setSelectedFile(file);
    if (!title) {
      const baseName = file.name.substring(0, file.name.lastIndexOf('.')) || file.name;
      setTitle(baseName.replace(/[_-]/g, ' '));
    }
  };

  const triggerUpload = () => {
    if (isLimitReached) return;
    if (fileInputRef.current) fileInputRef.current.click();
  };

  const removeFile = () => {
    setSelectedFile(null);
    setTitle('');
    setDescription('');
    setProgress(0);
    setIsUploading(false);
  };

  const handleUploadSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedFile) return;
    if (isLimitReached) {
      alert("Ingestion Limit Reached: Please contact opsfusionn@gmail.com");
      return;
    }

    setIsUploading(true);
    setProgress(0);

    const formData = new FormData();
    formData.append('file', selectedFile);

    const metadata = { title, description };
    formData.append('metadata', new Blob([JSON.stringify(metadata)], {
      type: 'application/json'
    }));

    const storedToken = localStorage.getItem('token');

    const xhr = new XMLHttpRequest();
    xhr.open('POST', 'http://localhost:8080/api/files/upload');
    if (storedToken) {
      xhr.setRequestHeader('Authorization', `Bearer ${storedToken}`);
    }

    // Monitor upload progress
    xhr.upload.onprogress = (event) => {
      if (event.lengthComputable) {
        const percentage = Math.round((event.loaded / event.total) * 100);
        setProgress(percentage);
      }
    };

    // On completion
    xhr.onload = () => {
      if (xhr.status >= 200 && xhr.status < 300) {
        setProgress(100);
        setTimeout(() => {
          setIsUploading(false);
          setSelectedFile(null);
          setTitle('');
          setDescription('');
          setProgress(0);
          if (onUploadSuccess) onUploadSuccess();
        }, 800);
      } else {
        setIsUploading(false);
        const errMsg = xhr.responseText.includes("Limit Reached") 
          ? "Ingestion Limit Reached: You have reached the maximum allowance of 3 active video jobs. To expand your ingestion limits, please contact us at opsfusionn@gmail.com."
          : `Ingestion failed with status code ${xhr.status}. Make sure the Spring Boot service is running.`;
        alert(errMsg);
      }
    };

    xhr.onerror = () => {
      setIsUploading(false);
      alert('Network error communicating with the StreamForge backend.');
    };

    xhr.send(formData);
  };

  return (
    <div className="relative overflow-hidden rounded-xl border border-white/5 bg-[#0a1020]/30 p-6 backdrop-blur-xl shadow-2xl">
      {/* Laser border lights */}
      <div className="absolute top-0 left-0 right-0 h-[1px] bg-gradient-to-r from-transparent via-[#ff6a28]/60 to-transparent" />
      
      {/* Background radial overlays */}
      <div className="absolute -top-32 -left-32 -z-10 h-72 w-72 rounded-full bg-[#ff6a28]/5 blur-3xl animate-pulse-glow" />
      <div className="absolute -bottom-32 -right-32 -z-10 h-72 w-72 rounded-full bg-[#34d1c4]/5 blur-3xl" />

      <div className="mb-6 flex items-center justify-between">
        <div>
          <h2 className="font-sans text-lg font-bold tracking-tight text-white">
            Ingest Raw Input
          </h2>
          <p className="text-xs text-[#a2aebf]">Trigger the video processing pipeline</p>
        </div>
        <div className="rounded bg-[#ff6a28]/5 border border-[#ff6a28]/20 px-2 py-0.5 font-mono text-[9px] font-bold text-[#ff6a28] uppercase">
          Asynchronous
        </div>
      </div>

      <form onSubmit={handleUploadSubmit} className="flex flex-col gap-6">
        
        {/* Drag active/inactive area */}
        {!selectedFile ? (
          isLimitReached ? (
            /* Limit Reached locked dropzone */
            <div
              className="group relative flex flex-col items-center justify-center border border-dashed border-[#ff5f56]/25 rounded-lg p-10 bg-[#ff5f56]/2 shadow-[0_0_20px_rgba(255,95,86,0.02)]"
            >
              <motion.div
                animate={{ scale: [1, 1.04, 1] }}
                transition={{ repeat: Infinity, duration: 4, ease: "easeInOut" }}
                className="mb-4 rounded-full bg-white/5 p-4 text-[#ff5f56] border border-white/5"
              >
                <ShieldAlert className="h-6 w-6 text-[#ff5f56] drop-shadow-[0_0_8px_rgba(255,95,86,0.5)]" />
              </motion.div>

              <span className="text-sm font-semibold text-white">Ingestion Limit Reached</span>
              <span className="text-xs text-[#636e7f] mt-2 text-center max-w-[280px] leading-relaxed">
                You have reached the maximum allowance of 3 active video jobs. To expand ingestion limits, please email us at{' '}
                <a href="mailto:opsfusionn@gmail.com" className="text-[#ff6a28] font-bold hover:underline">
                  opsfusionn@gmail.com
                </a>
              </span>
            </div>
          ) : (
            <div
              onDragEnter={handleDrag}
              onDragOver={handleDrag}
              onDragLeave={handleDrag}
              onDrop={handleDrop}
              onClick={triggerUpload}
              className={`group relative flex flex-col items-center justify-center border border-dashed rounded-lg p-10 cursor-pointer transition-all duration-300 ${
                dragActive 
                  ? 'border-[#ff6a28] bg-[#ff6a28]/5 shadow-[0_0_20px_rgba(255,106,40,0.05)]' 
                  : 'border-white/10 bg-black/20 hover:border-white/20 hover:bg-white/1'
              }`}
            >
              <input
                type="file"
                ref={fileInputRef}
                onChange={handleFileChange}
                className="hidden"
                accept="video/mp4,video/mkv"
              />
              
              {/* Floating arrow animation */}
              <motion.div
                animate={{ y: [0, -4, 0] }}
                transition={{ repeat: Infinity, duration: 4, ease: "easeInOut" }}
                className="mb-4 rounded-full bg-white/5 p-4 text-[#636e7f] group-hover:text-white transition-colors border border-white/5"
              >
                <Upload className="h-6 w-6 text-[#ff6a28] drop-shadow-[0_0_8px_#ff6a28]" />
              </motion.div>

              <span className="text-sm font-semibold text-white">Drag & drop raw MP4 / MKV input</span>
              <span className="text-xs text-[#636e7f] mt-1">or click to browse local filesystem</span>
            </div>
          )
        ) : (
          /* File info & progress ring */
          <div className="flex items-center justify-between rounded-lg bg-[#050816]/60 border border-white/5 p-4">
            <div className="flex items-center gap-4">
              <div className="relative flex h-16 w-16 items-center justify-center">
                {isUploading ? (
                  <>
                    <svg className="absolute w-full h-full transform -rotate-90">
                      <circle cx="32" cy="32" r={normalizedRadius} className="stroke-white/5 fill-none" strokeWidth={stroke} />
                      <circle 
                        cx="32" 
                        cy="32" 
                        r={normalizedRadius} 
                        className="stroke-[#ff6a28] fill-none transition-all duration-300" 
                        strokeWidth={stroke} 
                        strokeDasharray={circumference}
                        strokeDashoffset={strokeDashoffset}
                        strokeLinecap="round"
                      />
                    </svg>
                    <span className="font-mono text-[10px] font-bold text-white z-10">{progress}%</span>
                  </>
                ) : (
                  <div className="flex h-10 w-10 items-center justify-center rounded-full bg-green-500/10 border border-green-500/20 text-[#2ecc71]">
                    <Check className="h-5 w-5" />
                  </div>
                )}
              </div>
              
              <div className="flex flex-col gap-0.5 overflow-hidden">
                <span className="font-mono text-xs font-semibold text-white truncate max-w-[200px]">
                  {selectedFile.name}
                </span>
                <span className="font-mono text-[9px] text-[#636e7f]">
                  {(selectedFile.size / (1024 * 1024)).toFixed(2)} MB
                </span>
              </div>
            </div>

            <button
              type="button"
              onClick={removeFile}
              disabled={isUploading}
              className="text-[#636e7f] hover:text-[#ff5f56] p-1.5 rounded-md hover:bg-white/5 transition-all"
            >
              <X className="h-4 w-4" />
            </button>
          </div>
        )}

        {/* Form fields */}
        <div className="grid grid-cols-1 gap-4">
          <div className="flex flex-col gap-1.5">
            <label className="font-mono text-[9px] font-bold uppercase tracking-wider text-[#636e7f]">
              Resource Title
            </label>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g. Ingestion Stream 4"
              required
              disabled={!selectedFile || isUploading}
              className="w-full rounded bg-[#050816]/60 border border-white/5 px-4 py-2.5 text-xs text-white focus:outline-none focus:border-[#ff6a28] focus:ring-1 focus:ring-[#ff6a28] disabled:opacity-40"
            />
          </div>

          <div className="flex flex-col gap-1.5">
            <label className="font-mono text-[9px] font-bold uppercase tracking-wider text-[#636e7f]">
              Ingestion Notes / Description
            </label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Details about video profile, framerate, or transcoding rules..."
              disabled={!selectedFile || isUploading}
              rows={3}
              className="w-full rounded bg-[#050816]/60 border border-white/5 px-4 py-2.5 text-xs text-white focus:outline-none focus:border-[#ff6a28] focus:ring-1 focus:ring-[#ff6a28] resize-none disabled:opacity-40"
            />
          </div>
        </div>

        {/* Spec details grid */}
        <div className="grid grid-cols-3 gap-2">
          <div className="flex flex-col gap-1 rounded bg-[#050816]/30 border border-white/5 p-2 text-center">
            <span className="font-mono text-[8px] text-[#636e7f] uppercase">Formats</span>
            <span className="text-[10px] font-bold text-white">MP4 / MKV</span>
          </div>
          <div className="flex flex-col gap-1 rounded bg-[#050816]/30 border border-white/5 p-2 text-center">
            <span className="font-mono text-[8px] text-[#636e7f] uppercase">Max File Size</span>
            <span className="text-[10px] font-bold text-white">5.0 GB</span>
          </div>
          <div className="flex flex-col gap-1 rounded bg-[#050816]/30 border border-white/5 p-2 text-center">
            <span className="font-mono text-[8px] text-[#636e7f] uppercase">Transcode Rate</span>
            <span className="text-[10px] font-bold text-white">~1:1 Ratio</span>
          </div>
        </div>

        {/* Submit */}
        <button
          type="submit"
          disabled={isLimitReached || !selectedFile || isUploading}
          className={`w-full font-mono text-xs font-bold py-3.5 rounded transition-all flex items-center justify-center gap-2 ${
            isLimitReached
              ? 'bg-white/5 text-[#636e7f] border border-white/5 cursor-not-allowed'
              : 'bg-[#ff6a28] text-black hover:bg-[#ff7d42] disabled:opacity-50 disabled:cursor-not-allowed hover:shadow-[0_4px_20px_rgba(255,106,40,0.25)]'
          }`}
        >
          {isUploading ? (
            <>
              <Loader2 className="h-4 w-4 animate-spin" />
              Ingesting Payload...
            </>
          ) : isLimitReached ? (
            'Upload Limit Reached (Max 3)'
          ) : (
            'Ingest Video Stream'
          )}
        </button>

      </form>
    </div>
  );
}
