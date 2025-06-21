import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h2>Produtos</h2>
    <div class="product-grid">
      <div *ngFor="let product of products" class="product-card">
        <img [src]="product.imageUrl" [alt]="product.name" />
        <h3>{{ product.name }}</h3>
        <p>R$ {{ product.price }}</p>
      </div>
    </div>
  `,
  styleUrls: ['./product-list.component.scss']
})
export class ProductListComponent implements OnInit {
  products: any[] = [];

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<any[]>('http://localhost:8080/products').subscribe(data => {
      this.products = data;
    });
  }
}
