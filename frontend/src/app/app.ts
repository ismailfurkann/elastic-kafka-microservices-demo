import { Component, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule, NgForm } from '@angular/forms';
import { finalize } from 'rxjs';

@Component({ selector: 'app-root', imports: [FormsModule], templateUrl: './app.html', styleUrl: './app.css' })
export class App {
  private readonly http = inject(HttpClient);
  user = { name: '', email: '' };
  order: { product: string; price: number | null } = { product: '', price: null };
  readonly userPending = signal(false);
  readonly orderPending = signal(false);
  readonly userMessage = signal('');
  readonly orderMessage = signal('');

  createUser(form: NgForm) {
    if (form.invalid || !this.user.name.trim() || this.userPending()) return;
    this.userPending.set(true);
    this.userMessage.set('');
    this.http.post('http://localhost:8081/api/users', {
      name: this.user.name.trim(), email: this.user.email.trim()
    }).pipe(finalize(() => this.userPending.set(false))).subscribe({
      next: () => { this.userMessage.set('User created successfully.'); form.resetForm(); },
      error: () => this.userMessage.set('Could not create user. Check user-service and try again.')
    });
  }

  createOrder(form: NgForm) {
    const price = this.order.price;
    if (form.invalid || !this.order.product.trim() || price === null ||
        !Number.isInteger(price) || price < 0 || price > 2147483647 || this.orderPending()) return;
    this.orderPending.set(true);
    this.orderMessage.set('');
    this.http.post('http://localhost:8082/api/orders', {
      product: this.order.product.trim(), price
    }).pipe(finalize(() => this.orderPending.set(false))).subscribe({
      next: () => { this.orderMessage.set('Order created successfully.'); form.resetForm(); },
      error: () => this.orderMessage.set('Could not create order. Check order-service and try again.')
    });
  }
}
