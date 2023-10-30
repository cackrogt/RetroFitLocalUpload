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
        return {'success':True,'message':'File uploaded successfully'}
    else:
        print("fileuploadedunsuccessfull;")
        return {'success':False,'message':'File upload unsuccessful.'}

if __name__ == '__main__':
    app.run(debug=True)