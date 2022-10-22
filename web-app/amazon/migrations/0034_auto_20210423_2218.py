# Generated by Django 3.2 on 2021-04-23 22:18

from django.db import migrations, models
import django.utils.timezone


class Migration(migrations.Migration):

    dependencies = [
        ('amazon', '0033_auto_20210423_2217'),
    ]

    operations = [
        migrations.AlterField(
            model_name='order',
            name='start_date',
            field=models.DateTimeField(default=django.utils.timezone.now),
        ),
        migrations.AlterField(
            model_name='package',
            name='ordered_date',
            field=models.DateTimeField(default=django.utils.timezone.now),
        ),
    ]