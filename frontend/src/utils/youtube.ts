/**
 * Get YouTube embed URL from a watch URL or video ID.
 * e.g. https://www.youtube.com/watch?v=VIDEO_ID -> https://www.youtube.com/embed/VIDEO_ID
 */
export function getEmbedUrl(urlOrId: string | null | undefined): string {
  if (!urlOrId || !urlOrId.trim()) {
    return '';
  }
  const s = urlOrId.trim();
  const watchMatch = s.match(/(?:youtube\.com\/watch\?v=|youtu\.be\/)([a-zA-Z0-9_-]{11})/);
  if (watchMatch) {
    return `https://www.youtube.com/embed/${watchMatch[1]}`;
  }
  if (/^[a-zA-Z0-9_-]{11}$/.test(s)) {
    return `https://www.youtube.com/embed/${s}`;
  }
  return s;
}
