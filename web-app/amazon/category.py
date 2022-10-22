from .models import Category

def showCategories(request):
    categories = Category.objects.all()

    return {'showCategories': categories}