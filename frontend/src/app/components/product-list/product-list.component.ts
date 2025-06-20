import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h2>Produtos</h2>
    <ul>
      <li *ngFor="let product of products">
        <strong>{{ product.name }}</strong> – R$ {{ product.price }}
      </li>
    </ul>
  `,
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