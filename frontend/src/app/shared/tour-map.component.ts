import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges, AfterViewInit, ElementRef, ViewChild } from '@angular/core';
import * as L from 'leaflet';

@Component({
  selector: 'app-tour-map',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="map-wrapper" [style.height]="height" [class.non-interactive]="!interactive">
      <div #mapContainer class="map-container"></div>
      <div class="map-error" *ngIf="errorMessage">
        <span>{{ errorMessage }}</span>
      </div>
    </div>
  `,
  styles: [`
    .map-wrapper {
      position: relative;
      isolation: isolate; /* contain Leaflet's internal z-indices (up to 700) so they don't stack above the sticky navbar */
      width: 100%;
      border-radius: 10px;
      border: 1px solid #e2e8f0;
      background-color: #f1f5f9;
      overflow: hidden;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
    }
    .map-container {
      width: 100%;
      height: 100%;
    }
    .map-wrapper.non-interactive .map-container {
      pointer-events: none;
    }
    .map-error {
      position: absolute;
      top: 0.5rem;
      left: 0.5rem;
      right: 0.5rem;
      padding: 0.5rem 0.75rem;
      background-color: rgba(239, 68, 68, 0.92);
      color: #ffffff;
      border-radius: 6px;
      font-size: 0.8rem;
      font-weight: 500;
      text-align: center;
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
      z-index: 1000;
    }
  `]
})
export class TourMapComponent implements AfterViewInit, OnChanges {
  @ViewChild('mapContainer') mapContainer!: ElementRef;
  @Input() routeGeoJson: string | null = null;
  @Input() fromLocation: string = '';
  @Input() toLocation: string = '';
  @Input() height: string = '24rem';
  @Input() interactive: boolean = true;

  errorMessage: string | null = null;

  private map: L.Map | null = null;
  private routeLayer: L.GeoJSON | null = null;

  ngAfterViewInit(): void {
    this.initMap();
  }

  ngOnChanges(): void {
    if (this.map) {
      this.updateRoute();
    }
  }

  private initMap(): void {
    // fix leaflet default icon path issue
    const iconDefault = L.icon({
      iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
      iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
      shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
      iconSize: [25, 41],
      iconAnchor: [12, 41],
      popupAnchor: [1, -34],
      shadowSize: [41, 41]
    });
    L.Marker.prototype.options.icon = iconDefault;

    this.map = L.map(this.mapContainer.nativeElement, {
      zoomControl: this.interactive,
      dragging: this.interactive,
      scrollWheelZoom: this.interactive,
      doubleClickZoom: this.interactive,
      boxZoom: this.interactive,
      keyboard: this.interactive,
      touchZoom: this.interactive,
      attributionControl: this.interactive
    }).setView([47.5, 13.5], 7);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors'
    }).addTo(this.map);

    this.updateRoute();
  }

  private updateRoute(): void {
    if (!this.map) return;

    if (this.routeLayer) {
      this.map.removeLayer(this.routeLayer);
      this.routeLayer = null;
    }

    this.errorMessage = null;

    if (this.routeGeoJson) {
      try {
        const geoJson = JSON.parse(this.routeGeoJson);
        this.routeLayer = L.geoJSON(geoJson, {
          style: { color: '#4f46e5', weight: 4 }
        }).addTo(this.map);
        const bounds = this.routeLayer.getBounds();
        if (bounds.isValid()) {
          this.map.fitBounds(bounds, { padding: [20, 20] });
        }
      } catch {
        this.errorMessage = 'Route could not be loaded';
      }
    }
  }
}
