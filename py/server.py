#coding: utf-8

# https://github.com/kurtmckee/feedparser/blob/f1dd1bb923ebfe6482fc2521c1f150b4032289ec/feedparser/sanitizer.py

import json
from datetime import datetime, date
from time import mktime, struct_time

import feedparser
from flask import Flask, request


feedparser._HTMLSanitizer.acceptable_elements = {
    'p', 'br', 'a', 'b', 'blockquote', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
    'img', 'pre', 'span', 'strike', 'strong', 'sub', 'sup', 'i',
    'table', 'tbody', 'td', 'tfoot', 'th', 'thead', 'tr', 'tt', 'u', 'ul'
}

# todo how to deal with video

feedparser._HTMLSanitizer.acceptable_attributes = {'href', 'src'}

feedparser._HTMLSanitizer.sanitize_style = lambda *args: ''

feedparser._HTMLSanitizer.acceptable_css_keywords = set()
feedparser._HTMLSanitizer.acceptable_css_properties = set()


def json_encoder(obj):
    if isinstance(obj, (datetime, date)):
        return obj.isoformat()

    if isinstance(obj, struct_time):
        dt = datetime.fromtimestamp(mktime(obj))
        return json_encoder(dt)

    if isinstance(obj, Exception):
        return repr(obj)

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
