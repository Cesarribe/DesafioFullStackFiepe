import { Routes } from '@angular/router';
import { ProductListComponent } from './components/product-list/product-list.component';

export const routes: Routes = [
  {
    path: '',
    component: ProductListComponent,
  },
  {
    path: 'products/new',
    loadComponent: () =>
      import('./components/product-form/product-form.component').then(m => m.ProductFormComponent)
  }
];
