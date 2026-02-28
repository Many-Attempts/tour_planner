import { Component, Input, OnChanges, AfterViewInit, ElementRef, ViewChild } from '@angular/core';
import * as L from 'leaflet';

@Component({
  selector: 'app-tour-map',
  standalone: true,
  template: `<div #mapContainer class="map-container"></div>`,
  styles: [`
    .map-container {
      width: 100%;
      height: 24rem;
      border: 2px solid #1f2937;
      background-color: #f3f4f6;
    }
  `]
})
export class TourMapComponent implements AfterViewInit, OnChanges {
  @ViewChild('mapContainer') mapContainer!: ElementRef;
  @Input() routeGeoJson: string | null = null;
  @Input() fromLocation: string = '';
  @Input() toLocation: string = '';

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
    // Fix Leaflet default icon path issue
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

    this.map = L.map(this.mapContainer.nativeElement).setView([47.5, 13.5], 7);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors'
    }).addTo(this.map);

    this.updateRoute();
  }

  private updateRoute(): void {
    if (!this.map) return;

    if (this.routeLayer) {
      this.map.removeLayer(this.routeLayer);
    }

    if (this.routeGeoJson) {
      try {
        const geoJson = JSON.parse(this.routeGeoJson);
        this.routeLayer = L.geoJSON(geoJson, {
          style: { color: '#1f2937', weight: 4 }
        }).addTo(this.map);
        this.map.fitBounds(this.routeLayer.getBounds(), { padding: [20, 20] });
      } catch {
        // GeoJSON parsing failed, keep default view
      }
    }
  }
}
