import { MediaType } from './models/post/model';

export async function mediaFromFile(
  file: File,
  maxEdge = 1080,
): Promise<{ dataUrl: string; mediaType: MediaType }> {
  if (file.type.startsWith('video/')) {
    if (file.size > 1_500_000) {
      throw new Error('That video is too large. Pick a clip under 1.5 MB, or share a photo.');
    }
    return { dataUrl: await readAsDataUrl(file), mediaType: 'VIDEO' };
  }
  if (!file.type.startsWith('image/')) {
    throw new Error('Select a photo or video.');
  }
  return { dataUrl: await compressImage(file, maxEdge), mediaType: 'IMAGE' };
}

function readAsDataUrl(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(String(reader.result));
    reader.onerror = () => reject(new Error('Could not read that file.'));
    reader.readAsDataURL(file);
  });
}

function compressImage(file: File, maxEdge: number): Promise<string> {
  if (file.type === 'image/gif') {
    return readAsDataUrl(file);
  }
  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(file);
    const img = new Image();
    img.onload = () => {
      URL.revokeObjectURL(url);
      const scale = Math.min(1, maxEdge / Math.max(img.width, img.height));
      const canvas = document.createElement('canvas');
      canvas.width = Math.max(1, Math.round(img.width * scale));
      canvas.height = Math.max(1, Math.round(img.height * scale));
      const ctx = canvas.getContext('2d');
      if (!ctx) {
        reject(new Error('Could not process that photo.'));
        return;
      }
      ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
      resolve(canvas.toDataURL('image/jpeg', 0.82));
    };
    img.onerror = () => {
      URL.revokeObjectURL(url);
      reject(new Error('Could not open that photo.'));
    };
    img.src = url;
  });
}

export function timeAgo(iso: string): string {
  const seconds = Math.max(0, Math.floor((Date.now() - new Date(iso).getTime()) / 1000));
  if (seconds < 60) {
    return 'JUST NOW';
  }
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) {
    return `${minutes} MINUTE${minutes === 1 ? '' : 'S'} AGO`;
  }
  const hours = Math.floor(minutes / 60);
  if (hours < 24) {
    return `${hours} HOUR${hours === 1 ? '' : 'S'} AGO`;
  }
  const days = Math.floor(hours / 24);
  if (days < 7) {
    return `${days} DAY${days === 1 ? '' : 'S'} AGO`;
  }
  const weeks = Math.floor(days / 7);
  if (weeks < 5) {
    return `${weeks} WEEK${weeks === 1 ? '' : 'S'} AGO`;
  }
  return new Date(iso).toLocaleDateString(undefined, {
    month: 'long',
    day: 'numeric',
  }).toUpperCase();
}
