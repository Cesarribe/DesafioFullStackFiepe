import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-relatorio',
  imports: [CommonModule],
  templateUrl: './relatorio.component.html',
  styleUrls: ['./relatorio.component.scss']
})
export class RelatorioComponent {
  produtos = [
    { name: 'Produto A', price: 30, stock: 10 },
    { name: 'Produto B', price: 50, stock: 0 },
    { name: 'Produto C', price: 70, stock: 3 }
  ];

  get totalProdutos(): number {
    return this.produtos.length;
  }

  get estoqueTotal(): number {
    return this.produtos.reduce((total, p) => total + p.stock, 0);
  }

  get valorEstoque(): number {
    return this.produtos.reduce((total, p) => total + (p.stock * p.price), 0);
  }
}
