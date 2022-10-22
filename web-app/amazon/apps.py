from django.apps import AppConfig
from django.db.models.signals import post_migrate



def default_category():
    from amazon.models import Category
    if Category.objects.all().count() == 0:
        Category.objects.create(title="Electronics & Accessories", slug="electronics-accessories")
        Category.objects.create(title="Gadgets & Accessories", slug="gadgets-accessories")
        Category.objects.create(title="Men & Women Clothes", slug="men-women-clothes")
        Category.objects.create(title="Kids & Babies Clothes", slug="kids-babies-clothes")
        Category.objects.create(title="Fashion & Beauty", slug="fashion-beauty")
        Category.objects.create(title="New Arrivals", slug="new-arrivals")
        Category.objects.create(title="Best Selling", slug="best-selling")

def default_products():
    from django.contrib.auth.models import User
    from amazon.models import Product, Category
    if Product.objects.all().count() == 0:
        c1 = Category.objects.get(title="Electronics & Accessories", slug="electronics-accessories")
        c2 = Category.objects.get(title="Gadgets & Accessories", slug="gadgets-accessories")
        c3 = Category.objects.get(title="Men & Women Clothes", slug="men-women-clothes")
        c4 = Category.objects.get(title="Kids & Babies Clothes", slug="kids-babies-clothes")
        c5 = Category.objects.get(title="Fashion & Beauty", slug="fashion-beauty")
        c6 = Category.objects.get(title="New Arrivals", slug="new-arrivals")
        c7 = Category.objects.get(title="Best Selling", slug="best-selling")
        Product.objects.create(
            title = "1080p HD Indoor/Outdoor Video Camera",
            slug = "camera",
            price = 40.05,
            img = "../static/amazon/img/product-9.jpg",
            category = c6,
            description ="with Color Night Vision, 2-Way Audio!"
        )
        Product.objects.create(
            title = "Men Elastic-Waist Travel Pant Quick Dry",
            slug = "men-pant",
            price = 99.05,
            img = "../static/amazon/img/product-6.jpg",
            category = c3,
            description ="Men Elastic-Waist Travel Pant Quick Dry"
        )
        Product.objects.create(
            title = "Electronic Organizer Bag, Accessories Storage",
            slug = "ele-bag",
            price = 20.05,
            img = "../static/amazon/img/product-8.jpg",
            category = c1,
            description ="Save everything!"
        )
        Product.objects.create(
            title = "Waterproof Lasting Powder Fashion Eye Makeup",
            slug = "eye-makeup",
            price = 109.05,
            img = "../static/amazon/img/product-7.jpg",
            category = c5,
            description ="Limited Edition Waterproof Lasting!"
        )
        Product.objects.create(
           title = "Aveeno Baby Gentle Wash & Shampoo",
            slug = "shampoo",
            price = 11.88,
            img = "../static/amazon/img/product-2.jpg",
            category = c4,
            description ="Get one for your cute baby!"
        )
        Product.objects.create(
            title = "Boys Girls Crocs Unisex-Child Classic Clog",
            slug = "clog",
            price = 22.46,
            img = "../static/amazon/img/product-1.jpg",
            category = c4,
            description ="Best selling on beach!"
        )
        Product.objects.create(
            title = "Amazon Smart Plug, works with Humans Device",
            slug = "smart-plug",
            price = 24.99,
            img = "../static/amazon/img/product-5.jpg",
            category = c7,
            description ="Can use in many different countries!"
        )

        Product.objects.create(
            title = "MIHOLL Women'sCasual Loose Blouse Shirts",
            slug = "casual-shirts",
            price = 94.99,
            img = "../static/amazon/img/product-4.jpg",
            category = c6,
            description ="Best Selling! Get one!"
        )

        Product.objects.create(
            title = "Women's Voyager Striped Cut Out Maxi Dress",
            slug = "women-dress",
            price = 99.45,
            img = "../static/amazon/img/product-3.jpg",
            category = c3,
            description ="Get one for your girlfriend!"
        )
        Product.objects.create(
            title = "Motor Trend DualFlex Rubber Floor Mats",
            slug = "accessories",
            price = 199.88,
            img = "../static/amazon/img/product-10.jpg",
            category = c2,
            description ="Get one for your new car!"
        )


def default_warehouse():
    from amazon.models import WareHouse
    WareHouse.objects.create(x=1, y=1)


def migrate_callback(sender, **kwargs):
    default_category()
    default_products()
    default_warehouse()

class AmazonConfig(AppConfig):
    name = 'amazon'

    def ready(self):
        post_migrate.connect(migrate_callback, sender=self)

