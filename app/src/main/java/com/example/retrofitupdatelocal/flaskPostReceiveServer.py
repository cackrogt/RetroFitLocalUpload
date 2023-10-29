from flask import Flask, request
from werkzeug.utils import secure_filename

app = Flask(__name__)

@app.route('/upload', methods=['POST'])
def upload():
    if request.method == 'POST' and 'file' in request.files:
        file = request.files['file']
        filename = secure_filename(file.filename)
        file.save('uploads/' + filename)
        print("fileuploadedsuccessfull;")
        return 'File uploaded successfully'
    else:
        print("fileuploadedunsuccessfull;")
        return 'no file uploaded'

if __name__ == '__main__':
    app.run(debug=True)