import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { App } from './app';

describe('App forms', () => {
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [App], providers: [provideHttpClient(), provideHttpClientTesting()] });
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('submits both forms to their APIs and prevents duplicate user requests', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    await fixture.whenStable();
    const root = fixture.nativeElement as HTMLElement;
    const fill = async (id: string, value: string) => {
      const input = root.querySelector<HTMLInputElement>('#' + id)!;
      input.value = value;
      input.dispatchEvent(new Event('input'));
      await fixture.whenStable();
      fixture.detectChanges();
    };
    fixture.detectChanges();
    expect(root.querySelectorAll('button:disabled').length).toBe(2);
    await fill('name', 'Integration User');
    await fill('email', 'integration@example.com');
    const forms = root.querySelectorAll('form');
    forms[0].dispatchEvent(new Event('submit', { cancelable: true }));
    forms[0].dispatchEvent(new Event('submit', { cancelable: true }));
    const user = http.expectOne('http://localhost:8081/api/users');
    expect(user.request.method).toBe('POST');
    expect(user.request.body).toEqual({ name: 'Integration User', email: 'integration@example.com' });
    user.flush({ type: 'USER_CREATED' });
    await fill('product', 'Integration Product');
    await fill('price', '42');
    forms[1].dispatchEvent(new Event('submit', { cancelable: true }));
    const order = http.expectOne('http://localhost:8082/api/orders');
    expect(order.request.body).toEqual({ product: 'Integration Product', price: 42 });
    order.flush('Unavailable', { status: 503, statusText: 'Unavailable' });
    fixture.detectChanges();
    expect(root.textContent).toContain('User created successfully.');
    expect(root.textContent).toContain('Could not create order.');
    expect(fixture.componentInstance.orderPending()).toBe(false);
  });
});

