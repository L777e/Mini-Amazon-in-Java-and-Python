# Generated by Django 3.2 on 2021-04-20 16:35

from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('amazon', '0026_auto_20210420_1629'),
    ]

    operations = [
        migrations.RemoveField(
            model_name='order',
            name='package',
        ),
        migrations.AddField(
            model_name='package',
            name='order',
            field=models.OneToOneField(null=True, on_delete=django.db.models.deletion.CASCADE, to='amazon.order'),
        ),
    ]
