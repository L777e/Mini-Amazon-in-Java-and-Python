from django.contrib import admin
from .models import Customer,Product,Category,OrderItem,Order,WareHouse,Package

admin.site.register(Customer)
admin.site.register(Category)
admin.site.register(OrderItem)
admin.site.register(Order)
admin.site.register(WareHouse)
admin.site.register(Product)
admin.site.register(Package)