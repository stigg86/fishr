#!/usr/bin/env python3
from flask import Flask, send_from_directory
import os

app = Flask(__name__)
DIR = os.path.dirname(os.path.abspath(__file__))

@app.route('/')
def index():
    return send_from_directory(DIR, 'index.html')

@app.route('/<path:filename>')
def static_files(filename):
    return send_from_directory(DIR, filename)

if __name__ == '__main__':
    print("🎣 FISHR running on http://0.0.0.0:3457")
    app.run(host='0.0.0.0', port=3457, debug=False)
