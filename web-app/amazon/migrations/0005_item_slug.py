# Generated by Django 3.1.5 on 2021-04-17 16:58

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('amazon', '0004_auto_20210417_0553'),
    ]

    operations = [
        migrations.AddField(
            model_name='item',
            name='slug',
            field=models.SlugField(default='title'),
        ),
    ]