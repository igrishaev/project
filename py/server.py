#coding: utf-8

import json
from datetime import datetime, date
from time import mktime, struct_time

import feedparser
from flask import Flask, request


def json_encoder(obj):
    if isinstance(obj, (datetime, date)):
        return obj.isoformat()

    if isinstance(obj, struct_time):
        dt = datetime.fromtimestamp(mktime(obj))
        return json_encoder(dt)

    raise TypeError ("Type %s not serializable" % type(obj))


def fetch_feed(url, etag=None, modified=None):
    feed = feedparser.parse(url, etag=etag, modified=modified)
    return json.dumps(feed, indent=4, default=json_encoder)


app = Flask(__name__)

@app.route('/')
def fetch_handler():

    url = request.args.get("url")
    etag = request.args.get("etag")
    modified = request.args.get("modified")

    feed = fetch_feed(url, etag=etag, modified=modified)
    return (feed, 200, {"Content-Type": "application/json"})


def main():
    app.run(host='0.0.0.0', port=5000)


if __name__ == "__main__":
    main()
