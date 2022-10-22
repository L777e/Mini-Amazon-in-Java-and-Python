from django.shortcuts import render, get_object_or_404, redirect
from django.http import HttpResponse, JsonResponse
from .models import *
from django.views.generic import ListView, DetailView
from django.contrib.auth.decorators import login_required
from amazon import cart
import json
import datetime
from django.contrib import messages
from .utils import cookieCart, cartData, send_msg

def cart(request):
    data = cartData(request)    

    cartItems = data['cartItems']
    order = data['order']
    items = data['items']
    context = {
        'items':items, 
        'order':order, 
        'cartItems':cartItems}
    
    return render(request, 'amazon/cart.html', context)

@login_required
def Myaccount(request):
    data = cartData(request)    
    cartItems = data['cartItems']
    order = data['order']
    items = data['items']
    customer, created = Customer.objects.get_or_create(name=request.user.username, email=request.user.email)
    packages = Package.objects.all().filter(customer=request.user.customer).order_by('ordered_date').reverse()
    orders = Order.objects.all().filter(customer=request.user.customer).order_by('start_date').reverse()
    context = {
        'packages':packages,
        'orders':orders,
        'cartItems':cartItems
    }
    return render(request, 'amazon/my-account.html',context)

class ItemDetailView(DetailView):
    model = Product
    template_name = "amazon/product-detail.html"
    def get_queryset(self):
        return Product.objects.all()



def product_detail(request, category_slug, slug):
    product = get_object_or_404(Product, slug=slug)
    data = cartData(request)
    cartItems = data['cartItems']
    order = data['order']
    items = data['items']
    related = Product.objects.all().filter(category=product.category)
    context = {
        'product':product,
        'cartItems':cartItems,
        'related':related
    }
    return render(request, 'amazon/product-detail.html', context)

def products(request):
    data = cartData(request)
    cartItems = data['cartItems']
    order = data['order']
    items = data['items']
    context = {
        'products': Product.objects.all(),
        'cartItems':cartItems
    }
    return render(request, "amazon/product-list.html", context)


def home(request):
	data = cartData(request)
	cartItems = data['cartItems']
	order = data['order']
	items = data['items']

	products = Product.objects.all()
	context = {'products':products, 'cartItems':cartItems}
	return render(request, 'amazon/index.html', context)


def checkout(request):
    data = cartData(request)

    cartItems = data['cartItems']
    order = data['order']
    items = data['items']
    context = {'items':items, 'order':order, 'cartItems':cartItems}

    if request.method == "POST":
        x = request.POST.get("address_x")
        y = request.POST.get("address_y")
        print('---------')
        print(x)
        print('---------')
        print(y)
        name = request.POST.get("name")
        email = request.POST.get("email")
        if "UPS_id" in request.POST.keys():
            UPS_id = request.POST.get("UPS_id")
        print('---------')
        print(name)
        print('---------')
        print(email)
        print('---------')
        print(UPS_id)
        if request.user.is_authenticated:
            customer = request.user.customer
            order, created = Order.objects.get_or_create(customer=customer, complete=False)
        else:
            cookieData = cookieCart(request)
            customer, created = Customer.objects.get_or_create(name=name, email=email)
            customer.name = name
            customer.save()
            cookieData = cookieCart(request)
            items = cookieData['items']

            order = Order.objects.create(customer=customer,complete=False)
            for item in items:
                product = Product.objects.get(id=item.get('id'))
                orderItem = OrderItem.objects.create(
                    product=product,
                    order=order,
                    quantity=(item['quantity'] if item['quantity']>0 else -1*item['quantity']), # negative quantity = freebies
                )

        package = Package(order=order, customer=customer, warehouse=1, dest_x=x, dest_y=y)

        order.complete = True
        order.save()   
        package.order.complete = True  
        package.save()
        # while send_to_ups(package.id) == False:
        send_msg(package.id)
        messages.success(request, f'Order created successfully! You can check on My account')
    return render(request, 'amazon/checkout.html', context)


def updateItem(request):
    data = json.loads(request.body)
    productId = data['productId']
    action = data['action']
    print('Action:', action)
    print('Product:', productId)

    customer = request.user.customer
    product = Product.objects.get(id=productId)
    order, created = Order.objects.get_or_create(customer=customer, complete=False)

    orderItem, created = OrderItem.objects.get_or_create(order=order, product=product)
    if created:
        orderItem.quantity = 0

    if action == 'add':
        orderItem.quantity = (orderItem.quantity + 1)
    elif action == 'remove':
        orderItem.quantity = (orderItem.quantity - 1)
    elif action == 'clear':
        orderItem.quantity = 0

    orderItem.save()
    
    if orderItem.quantity <= 0:
        orderItem.delete()

    return JsonResponse('Item was added', safe=False)

def category_detail(request, slug):
    data = cartData(request)
    cartItems = data['cartItems']
    order = data['order']
    items = data['items']
    category = get_object_or_404(Category, slug=slug)
    products = category.products.all()
    context = {
        'category':category,
        'products':products,
        'cartItems':cartItems
    }
    return render(request, 'amazon/category-detail.html', context)






