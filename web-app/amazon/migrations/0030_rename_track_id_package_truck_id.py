# Generated by Django 3.2 on 2021-04-22 14:37

from django.db import migrations


class Migration(migrations.Migration):

    dependencies = [
        ('amazon', '0029_product_quantity'),
    ]

    operations = [
        migrations.RenameField(
            model_name='package',
            old_name='track_id',
            new_name='truck_id',
        ),
    ]
