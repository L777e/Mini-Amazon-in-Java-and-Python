from django.db import models
from django.conf import settings
from django.contrib.auth.models import User
from django.utils.timezone import now
from django.utils import timezone

# owner of orders and packages
class Customer(models.Model):
	user = models.OneToOneField(User, null=True, blank=True, on_delete=models.CASCADE, related_name="customer")
	name = models.CharField(max_length=200, null=True)
	email = models.CharField(max_length=200)

	def __str__(self):
		return self.name
# default profile of a user that can be edited
class UserProfile(models.Model):
    user = models.OneToOneField(
        settings.AUTH_USER_MODEL, on_delete=models.CASCADE)
    stripe_customer_id = models.CharField(max_length=50, blank=True, null=True)
    one_click_purchasing = models.BooleanField(default=False)

    def __str__(self):
        return self.user.username
# default warehouse at (x=1,y=1)
class WareHouse(models.Model):
    x = models.IntegerField(default=1)
    y = models.IntegerField(default=1)
    def __str__(self):
        return "<" + str(self.x) + ", " + str(self.y) + ">"

# categories for all products
class Category(models.Model):
    title = models.CharField(max_length=255, default="")
    slug = models.SlugField(max_length=255, default="")

    def __str__(self):
        return self.title

class Product(models.Model):
    title = models.CharField(max_length=255)
    slug = models.SlugField(max_length=255, default="")
    price = models.FloatField()
    img = models.ImageField(default = "product-default.jpg", null=True, blank = True)
    category = models.ForeignKey(Category, related_name = 'products', on_delete=models.CASCADE, null=True)
    quantity = models.IntegerField(default = 1)
    description = models.TextField(blank=True, null=True, default = "")

    def __str__(self):
        return self.title
    
    @property
    def imageURL(self):
        try:
            url = self.img.url
        except:
            url = ''
        return url


#used for change the user profile
class Post(models.Model):
    title = models.CharField(max_length=100)
    content = models.TextField()
    data_posted = models.DateTimeField(default=now)
    author = models.ForeignKey(User, on_delete=models.CASCADE)

#created when items are added into the cart
class Order(models.Model):
    customer = models.ForeignKey(Customer, on_delete=models.SET_NULL, null=True, blank=True)
    start_date = models.DateTimeField(default=now)
    complete = models.BooleanField(default=False)

    def __str__(self):
        return str(self.id)

    # git total price of the items in cart
    @property
    def get_cart_total(self):
        orderitems = self.orderitem_set.all()
        total = sum([item.get_total for item in orderitems])
        return total 

    # git total quantity of the items in cart
    @property
    def get_cart_items(self):
        orderitems = self.orderitem_set.all()
        total = sum([item.quantity for item in orderitems])
        return total 

    def get_start_date(self):
        return self.start_date

     # git list of all products
    @property
    def get_products(self):
        orderitems = self.orderitem_set.all()
        total = ""
        for item in orderitems:
            itemStr = str(item) + "\n"
            print(itemStr)
            total = total + itemStr
        return total

    def get_status(self):
        if self.complete:
            return "True"
        else:
            return "False"


        
class OrderItem(models.Model):
    order = models.ForeignKey(Order, on_delete=models.SET_NULL, null=True)
    product = models.ForeignKey(Product, on_delete=models.CASCADE)
    quantity = models.IntegerField(default=1)

    #get list of all items and their quantity
    def __str__(self):
        return "<" + str(self.product) + "," + str(self.quantity) + ">"

    #get total price of one orderitem
    @property
    def get_total(self):
        total = self.product.price * self.quantity
        return total
    
    def get_orderitem(self):
        return + self.product + " " + self.product.quantity

    def get_order_id(self):
        return self.order


class Package(models.Model):
    customer = models.ForeignKey(Customer, on_delete=models.CASCADE, default="")
    warehouse = models.IntegerField(default=1)
    # products = models.ForeignKey(OrderItem, on_delete=models.CASCADE, null=True)
    order = models.OneToOneField(Order, on_delete=models.CASCADE, null=True)
    status = models.CharField(max_length=100,default="processing")
    dest_x = models.IntegerField(default=1)
    dest_y = models.IntegerField(default=1)
    ordered_date = models.DateTimeField(default=now)
    truck_id = models.CharField(max_length=100, null=True)
    UPS_id = models.CharField(max_length=100, null=True)

    def get_order_num(self):
        return self.order

    def get_date(self):
        return self.ordered_date

    def get_total_price(self):
        return self.order.get_cart_total

    def get_status(self):
        return self.status

    def get_products(self):
        return self.order.get_products

    def __str__(self):
        return "<" + str(self.id) + "," +str(self.customer) + "," + str(self.order) + "," + str(self.dest_x) + "," + str(self.dest_y) + ">"