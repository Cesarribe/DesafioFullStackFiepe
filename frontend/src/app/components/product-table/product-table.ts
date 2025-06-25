import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { Router } from '@angular/router';
import { Product } from '../../models/product.model';
import { ProductService } from '../../services/product.service';

@Component({
  selector: 'app-product-table',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './product-table.html',
  styleUrls: ['./product-table.scss']
})
export class ProductTableComponent implements OnInit {
  produtos: Product[] = [];
  filtros = { name: '', category: '', estoqueMin: '' };

  constructor(
    private productService: ProductService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.filtrar();
  }

  filtrar(): void {
    const params: any = {};
    if (this.filtros.name) params.name = this.filtros.name;
    if (this.filtros.category) params.category = this.filtros.category;
    if (this.filtros.estoqueMin) params.estoqueMin = this.filtros.estoqueMin;
    this.productService.listarProdutos(params).subscribe({
      next: (products: Product[]) => this.produtos = products,
      error: (error: any) => console.error('Erro ao filtrar:', error)
    });
  }

  aplicarDesconto(produto: Product): void {
    const entrada = prompt(`% de desconto para "${produto.name}":`);
    const percent = Number(entrada);
    if (isNaN(percent) || percent <= 0 || percent > 90) {
      alert('Insira valor entre 1 e 90');
      return;
    }
    this.productService.aplicarDescontoPercentual(produto.id!, percent).subscribe({
      next: () => this.filtrar(),
      error: (error: any) => console.error('Erro ao aplicar desconto:', error)
    });
  }

  excluirProduto(produto: Product): void {
    if (!confirm(`Excluir "${produto.name}"?`)) return;
    this.productService.excluirProduto(produto.id!).subscribe({
      next: () => this.filtrar(),
      error: (error: any) => console.error('Erro ao excluir:', error)
    });
  }

  editarProduto(produto: Product): void {
    this.router.navigate(['/cadastro'], { state: { produto } });
  }

  venderProduto(produto: Product): void {
    if (produto.stock <= 0) {
      alert('Estoque esgotado');
      return;
    }
    this.productService
      .atualizarEstoque(produto.id!, produto.stock - 1)
      .subscribe({
        next: (updated: Product) => {
          produto.stock = updated.stock;
          alert('Venda realizada com sucesso!');
          this.filtrar();
        },
        error: (error: any) => {
          console.error('Erro ao vender:', error);
          alert('Não foi possível concluir a venda.');
        }
      });
  }
}
