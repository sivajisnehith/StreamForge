'use client';

import React, { useEffect, useRef } from 'react';
import * as THREE from 'three';

export default function ThreeLanding() {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    if (!canvasRef.current) return;

    const canvas = canvasRef.current;
    let width = canvas.parentElement?.clientWidth || window.innerWidth;
    let height = canvas.parentElement?.clientHeight || window.innerHeight;

    const renderer = new THREE.WebGLRenderer({ canvas, antialias: true, alpha: true });
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    renderer.setSize(width, height);

    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(45, width / height, 0.1, 100);
    camera.position.set(0, 0.6, 11);

    const emberColor = new THREE.Color(0xff6a28);
    const signalColor = new THREE.Color(0x34d1c4);

    const group = new THREE.Group();
    group.position.set(2.4, -0.2, 0);
    scene.add(group);

    // Raw input cube
    const inputGeo = new THREE.BoxGeometry(1.1, 1.1, 1.1);
    const inputMat = new THREE.MeshStandardMaterial({ color: 0x2a3038, roughness: 0.4, metalness: 0.3, emissive: 0x0d0f12 });
    const inputCube = new THREE.Mesh(inputGeo, inputMat);
    inputCube.position.set(-4.2, 0, 0);
    group.add(inputCube);
    const inputWire = new THREE.LineSegments(new THREE.EdgesGeometry(inputGeo), new THREE.LineBasicMaterial({ color: 0x556070 }));
    inputCube.add(inputWire);

    // Glowing core
    const coreGeo = new THREE.IcosahedronGeometry(1.15, 1);
    const coreMat = new THREE.MeshBasicMaterial({ color: emberColor, wireframe: true, transparent: true, opacity: 0.85 });
    const core = new THREE.Mesh(coreGeo, coreMat);
    core.position.set(-0.6, 0, 0);
    group.add(core);

    const coreGlowGeo = new THREE.IcosahedronGeometry(1.5, 1);
    const coreGlowMat = new THREE.MeshBasicMaterial({ color: emberColor, transparent: true, opacity: 0.06 });
    const coreGlow = new THREE.Mesh(coreGlowGeo, coreGlowMat);
    core.add(coreGlow);

    const corePoint = new THREE.PointLight(0xff6a28, 3.2, 9);
    corePoint.position.set(-0.6, 0, 0.5);
    group.add(corePoint);

    const signalPoint = new THREE.PointLight(0x34d1c4, 2.2, 9);
    signalPoint.position.set(3.2, 0, 1);
    group.add(signalPoint);

    scene.add(new THREE.AmbientLight(0x404850, 1.2));

    // Output HLS layers (1080p, 720p, 480p)
    const outputs: THREE.Mesh[] = [];
    const outputSpecs = [
      { h: 1.5, y: 1.35, label: '1080p' },
      { h: 1.05, y: 0,    label: '720p'  },
      { h: 0.7,  y: -1.35, label: '480p' }
    ];

    outputSpecs.forEach((spec, i) => {
      const geo = new THREE.PlaneGeometry(1.6, spec.h);
      const mat = new THREE.MeshStandardMaterial({
        color: signalColor, transparent: true, opacity: 0.5 - i * 0.06,
        side: THREE.DoubleSide, roughness: 0.5, metalness: 0.2, emissive: 0x0b2b28
      });
      const plane = new THREE.Mesh(geo, mat);
      plane.position.set(3.6, spec.y, 0);
      plane.userData = { baseY: spec.y, phase: i * 1.3 };
      group.add(plane);
      const edge = new THREE.LineSegments(new THREE.EdgesGeometry(geo), new THREE.LineBasicMaterial({ color: 0x6fe6dc, transparent: true, opacity: 0.5 }));
      plane.add(edge);
      outputs.push(plane);
    });

    // Connector paths
    function makeBeam(from: THREE.Vector3, to: THREE.Vector3, color: number) {
      const points = [from, to];
      const geo = new THREE.BufferGeometry().setFromPoints(points);
      const mat = new THREE.LineBasicMaterial({ color, transparent: true, opacity: 0.35 });
      return new THREE.Line(geo, mat);
    }
    group.add(makeBeam(new THREE.Vector3(-3.6, 0, 0), new THREE.Vector3(-1.6, 0, 0), 0x556070));
    outputSpecs.forEach(spec => {
      group.add(makeBeam(new THREE.Vector3(0.3, 0, 0), new THREE.Vector3(2.9, spec.y * 0.7, 0), 0x2f7c76));
    });

    // Particle embers stream
    const PARTICLE_COUNT = 90;
    const particleGeo = new THREE.BufferGeometry();
    const positions = new Float32Array(PARTICLE_COUNT * 3);
    const seeds = new Float32Array(PARTICLE_COUNT);
    for (let i = 0; i < PARTICLE_COUNT; i++) {
      seeds[i] = Math.random();
      positions[i * 3] = -4.2;
      positions[i * 3 + 1] = (Math.random() - 0.5) * 0.6;
      positions[i * 3 + 2] = (Math.random() - 0.5) * 0.6;
    }
    particleGeo.setAttribute('position', new THREE.BufferAttribute(positions, 3));
    const particleMat = new THREE.PointsMaterial({ color: 0xffb27a, size: 0.05, transparent: true, opacity: 0.85 });
    const particles = new THREE.Points(particleGeo, particleMat);
    group.add(particles);

    function updateParticles(t: number) {
      const pos = particleGeo.attributes.position.array as Float32Array;
      for (let i = 0; i < PARTICLE_COUNT; i++) {
        const localT = (t * 0.15 + seeds[i]) % 1;
        const targetIdx = i % 3;
        const targetY = outputSpecs[targetIdx].y;
        let x, y, z;
        if (localT < 0.35) {
          const lt = localT / 0.35;
          x = THREE.MathUtils.lerp(-4.2, -0.6, lt);
          y = THREE.MathUtils.lerp((seeds[i] - 0.5) * 0.6, 0, lt);
          z = (seeds[i] - 0.5) * 0.4;
        } else {
          const lt = (localT - 0.35) / 0.65;
          x = THREE.MathUtils.lerp(-0.6, 3.6, lt);
          y = THREE.MathUtils.lerp(0, targetY, lt);
          z = 0;
        }
        pos[i * 3] = x;
        pos[i * 3 + 1] = y;
        pos[i * 3 + 2] = z;
      }
      particleGeo.attributes.position.needsUpdate = true;
    }

    // Parallax
    let mouseX = 0, mouseY = 0;
    const handleMouseMove = (e: MouseEvent) => {
      mouseX = (e.clientX / window.innerWidth - 0.5);
      mouseY = (e.clientY / window.innerHeight - 0.5);
    };
    window.addEventListener('mousemove', handleMouseMove);

    let frame = 0;
    let animationFrameId: number;

    const animate = () => {
      animationFrameId = requestAnimationFrame(animate);
      frame++;
      const t = frame / 60;

      core.rotation.y += 0.006;
      core.rotation.x += 0.002;
      updateParticles(t);
      outputs.forEach(p => {
        p.position.y = p.userData.baseY + Math.sin(t * 0.8 + p.userData.phase) * 0.08;
      });
      group.rotation.y += (mouseX * 0.3 - group.rotation.y) * 0.02;
      group.rotation.x += (mouseY * 0.15 - group.rotation.x) * 0.02;

      renderer.render(scene, camera);
    };
    animate();

    // Resize
    const onResize = () => {
      if (!canvasRef.current || !canvasRef.current.parentElement) return;
      width = canvasRef.current.parentElement.clientWidth;
      height = canvasRef.current.parentElement.clientHeight || window.innerHeight;
      camera.aspect = width / height;
      camera.updateProjectionMatrix();
      renderer.setSize(width, height);

      // Parallax scale adjusting for responsiveness
      if (width < 860) {
        group.position.set(0, -1.8, 0);
        group.scale.setScalar(0.75);
      } else if (width < 1100) {
        group.position.set(1.5, -0.2, 0);
        group.scale.setScalar(0.9);
      } else {
        group.position.set(2.4, -0.2, 0);
        group.scale.setScalar(1.0);
      }
    };
    window.addEventListener('resize', onResize);
    onResize();

    // Clean up
    return () => {
      cancelAnimationFrame(animationFrameId);
      window.removeEventListener('mousemove', handleMouseMove);
      window.removeEventListener('resize', onResize);

      inputGeo.dispose();
      inputMat.dispose();
      inputWire.geometry.dispose();
      (inputWire.material as THREE.Material).dispose();

      coreGeo.dispose();
      coreMat.dispose();
      coreGlowGeo.dispose();
      coreGlowMat.dispose();

      corePoint.dispose();
      signalPoint.dispose();

      outputs.forEach(plane => {
        plane.geometry.dispose();
        if (Array.isArray(plane.material)) {
          plane.material.forEach(m => m.dispose());
        } else {
          plane.material.dispose();
        }
        plane.children.forEach(c => {
          if (c instanceof THREE.LineSegments) {
            c.geometry.dispose();
            (c.material as THREE.Material).dispose();
          }
        });
      });

      particleGeo.dispose();
      particleMat.dispose();

      renderer.dispose();
    };
  }, []);

  return <canvas ref={canvasRef} className="absolute inset-0 w-full h-full pointer-events-none" />;
}
