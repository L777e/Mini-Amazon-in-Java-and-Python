# Generated by Django 3.2 on 2021-04-19 21:40

from django.db import migrations


class Migration(migrations.Migration):

    dependencies = [
        ('amazon', '0023_auto_20210419_2056'),
    ]

    operations = [
        migrations.RemoveField(
            model_name='package',
            name='products',
        ),
    ]