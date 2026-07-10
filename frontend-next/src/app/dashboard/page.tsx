'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import Navbar from '@/components/Navbar';
import WelcomeHero from '@/components/WelcomeHero';
import StatsGrid from '@/components/StatsGrid';
import UploadCard from '@/components/UploadCard';
import ProcessingPipeline from '@/components/ProcessingPipeline';
import VideoTable from '@/components/VideoTable';
import WorkerCluster from '@/components/WorkerCluster';
import StorageAnalytics from '@/components/StorageAnalytics';
import RecentActivity from '@/components/RecentActivity';
import Footer from '@/components/Footer';
import HlsPlayerModal from '@/components/HlsPlayerModal';

export default function DashboardPage() {
  const router = useRouter();
  const [videos, setVideos] = useState<any[]>([]);
  const [token, setToken] = useState<string | null>(null);
  const [activeStreamUrl, setActiveStreamUrl] = useState<string | null>(null);
  const [activeStreamTitle, setActiveStreamTitle] = useState<string>('');
  const [searchQuery, setSearchQuery] = useState<string>('');

  // Authenticate token on mount
  useEffect(() => {
    const storedToken = localStorage.getItem('token');
    if (!storedToken) {
      window.location.href = '/login.html';
    } else {
      setToken(storedToken);
    }
  }, []);

  // Fetch videos from Spring Boot endpoint
  const fetchVideos = useCallback(async (query: string = '') => {
    const storedToken = localStorage.getItem('token');
    if (!storedToken) return;

    try {
      const url = `http://localhost:8080/api/videos?size=100${
        query ? `&search=${encodeURIComponent(query)}` : ''
      }`;
      const response = await fetch(url, {
        headers: {
          'Authorization': `Bearer ${storedToken}`
        }
      });

      if (response.status === 401) {
        localStorage.removeItem('token');
        window.location.href = '/login.html';
        return;
      }

      if (!response.ok) throw new Error('API Error');

      const data = await response.json();
      setVideos(data.content || []);
    } catch (e) {
      console.error('Failed to fetch videos from backend:', e);
    }
  }, []);

  // Debounce search input and fetch
  useEffect(() => {
    const delayDebounce = setTimeout(() => {
      fetchVideos(searchQuery);
    }, 300);

    return () => clearTimeout(delayDebounce);
  }, [searchQuery, fetchVideos]);

  // Initial fetch when token is authenticated
  useEffect(() => {
    if (token) {
      fetchVideos();
    }
  }, [token, fetchVideos]);

  // Set up polling interval if there are active queue jobs
  useEffect(() => {
    if (!token || videos.length === 0) return;

    const hasActiveJobs = videos.some((v) => 
      ['UPLOADED', 'PENDING', 'PROCESSING'].includes(v.status)
    );

    if (hasActiveJobs) {
      const interval = setInterval(() => {
        fetchVideos(searchQuery);
      }, 4000);
      return () => clearInterval(interval);
    }
  }, [videos, token, fetchVideos, searchQuery]);

  // Extract count of processing queues
  const processingCount = videos.filter((v) =>
    ['UPLOADED', 'PENDING', 'PROCESSING'].includes(v.status)
  ).length;

  return (
    <div className="flex min-h-screen flex-col bg-[#050816]">
      {/* Background neon lights */}
      <div className="absolute top-0 left-1/4 -z-10 h-[500px] w-[500px] rounded-full bg-purple-900/5 blur-[140px] animate-pulse-glow" />
      <div className="absolute top-[20%] right-10 -z-10 h-[600px] w-[600px] rounded-full bg-blue-900/5 blur-[150px]" />
      
      {/* Header bar */}
      <Navbar />

      {/* Grid wrapper */}
      <main className="mx-auto flex w-full max-w-7xl flex-grow flex-col gap-8 px-6 py-4">
        {/* Welcome & Overview */}
        <div id="overview" className="scroll-mt-24">
          <WelcomeHero processingCount={processingCount} />
          <div className="mt-8">
            <StatsGrid videos={videos} />
          </div>
        </div>

        {/* Ingestion Pipeline Split layout */}
        <section id="pipeline" className="grid grid-cols-1 gap-8 lg:grid-cols-3 scroll-mt-24">
          <div className="flex flex-col gap-8 lg:col-span-2">
            <UploadCard onUploadSuccess={fetchVideos} existingCount={videos.length} />
            <ProcessingPipeline videos={videos} />
          </div>
          <div className="flex flex-col gap-8 lg:col-span-1">
            <RecentActivity videos={videos} />
          </div>
        </section>

        {/* Workers Cluster */}
        <div id="workers" className="scroll-mt-24">
          <WorkerCluster videos={videos} />
        </div>

        {/* Storage Analytics Charts */}
        <div id="analytics" className="scroll-mt-24">
          <StorageAnalytics videos={videos} />
        </div>

        {/* Videos Database table */}
        <div id="database" className="scroll-mt-24">
          <VideoTable 
            videos={videos} 
            onRefresh={fetchVideos} 
            onStream={(url, title) => {
              setActiveStreamUrl(url);
              setActiveStreamTitle(title);
            }}
            searchQuery={searchQuery}
            onSearchChange={setSearchQuery}
          />
        </div>
      </main>

      {/* HLS Player Modal Overlay */}
      {activeStreamUrl && (
        <HlsPlayerModal
          streamUrl={activeStreamUrl}
          title={activeStreamTitle}
          onClose={() => {
            setActiveStreamUrl(null);
            setActiveStreamTitle('');
          }}
        />
      )}
 
      {/* Footer */}
      <Footer />
    </div>
  );
}
