FROM python:3
ENV PYTHONUNBUFFERED 1
RUN mkdir /code
WORKDIR /code
ADD requirements.txt /code/
RUN pip install -r requirements.txt
RUN pip install django-crispy-forms Pillow django-createsuperuser
ADD . /code/
RUN chmod 777 ./amazon/static/amazon/img
RUN chmod 777 ./media/

