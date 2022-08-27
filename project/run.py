from flask import Flask
import aiai
from flask import Flask, request
from PIL import Image
from werkzeug.utils import secure_filename
import numpy
app = Flask(__name__)

@app.route('/')
def home():
    return 'This is home!'

@app.route('/image', methods=['POST'])
def image():
    if request.method == "POST":
        f = request.files['file']
        pil_image = Image.open(f.stream).convert('RGB') 
        open_cv_image = numpy.array(pil_image) 
        # Convert RGB to BGR 
        open_cv_image = open_cv_image[:, :, ::-1].copy() 
        #return aiai.loadImage(open_cv_image)
        return {"res":aiai.loadImage(open_cv_image)}


if __name__ == '__main__':
    app.run('0.0.0.0', port=5000, debug=True)