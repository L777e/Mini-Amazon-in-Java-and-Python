from django.urls import path
from .import views
from django.shortcuts import reverse
from .views import *

urlpatterns = [
    path('', views.home, name = 'Amazon-home'),
    path('product/', views.products, name = 'Amazon-products'),
    path('checkout/', views.checkout, name = 'checkout'),
    path('cart/', views.cart, name = 'cart'),
    path('my_account/', views.Myaccount, name = 'my_account'),
	path('update_item/', views.updateItem, name="update_item"),
]
