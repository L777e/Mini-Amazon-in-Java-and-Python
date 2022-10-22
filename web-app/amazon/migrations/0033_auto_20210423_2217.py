# Generated by Django 3.2 on 2021-04-23 22:17

import datetime
from django.db import migrations, models
from django.utils.timezone import utc


class Migration(migrations.Migration):

    dependencies = [
        ('amazon', '0032_auto_20210423_2212'),
    ]

    operations = [
        migrations.AlterField(
            model_name='order',
            name='start_date',
            field=models.DateTimeField(default=datetime.datetime(2021, 4, 23, 22, 17, 2, 219600, tzinfo=utc)),
        ),
        migrations.AlterField(
            model_name='package',
            name='ordered_date',
            field=models.DateTimeField(default=datetime.datetime(2021, 4, 23, 22, 17, 2, 220564, tzinfo=utc)),
        ),
    ]