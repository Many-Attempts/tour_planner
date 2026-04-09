export function formatTime(seconds: number | null | undefined): string {
  if (!seconds) return '-';
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  if (hours > 0) return `${hours}h ${minutes}m`;
  return `${minutes}m`;
}

export function formatDistance(km: number | null | undefined): string {
  if (km === null || km === undefined) return '-';
  return `${km.toFixed(1)} km`;
}

export function formatDate(dateStr: string | null | undefined): string {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return date.toLocaleDateString('en-US', { year: 'numeric', month: '2-digit', day: '2-digit' });
}

export function formatTimeFromDate(dateStr: string | null | undefined): string {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: false });
}

export function getTransportLabel(type: string): string {
  const labels: Record<string, string> = {
    CAR: 'Car',
    BICYCLE: 'Bike',
    WALKING: 'Walking',
    RUNNING: 'Running',
    HIKING: 'Hiking'
  };
  return labels[type] || type;
}

export function getStarArray(count: number): number[] {
  return Array.from({ length: count }, (_, i) => i + 1);
}
