from flask import Flask, request, abort, Response
import subprocess

MAX_THUMB_LENGTH = 3840

app =  Flask(__name__)

@app.route('/resize', methods=['POST'])
def resize():
    def validated_data():
        width = int(request.form['width']) if request.form['width'] is not None else None
        height = int(request.form['height']) if request.form['height'] is not None else None
        data = request.files['data']
        if (width is None or height is None):
            raise ValueError("Both of width and height are required.")
        if (width <= 0 or width > MAX_THUMB_LENGTH):
            raise ValueError("The value of width is invalid. A width must be (0, {0}]. (The value was {1}.)".format(MAX_THUMB_LENGTH, width))
        if (height is None or height <= 0 or height > MAX_THUMB_LENGTH):
            raise ValueError("The value of height is invalid. A height must be (0, {0}]. (The value was {1}.)".format(MAX_THUMB_LENGTH, height))
        if (data is None):
            raise ValueError("An image data is required.")
        return (width, height, data)

    try:
        (width, height, data) = validated_data()
        command = 'convert - -resize {0}x{1} png:-'.format(width, height)
        process = subprocess.Popen(command.split(" "), stdin=subprocess.PIPE, stdout=subprocess.PIPE)
        process.stdin.write(data.read())
        process.stdin.close()
        converted = process.stdout.read()
        code = process.wait()

        if (code == 0):
            return Response(converted, mimetype='image/png')
        else:
            app.logger.error("Failed to convert.")
            return abort(500)
    # バリデーションに失敗したとき
    except ValueError as e:
        app.logger.info(e)
        return abort(400)
    except OSError as e:
        app.logger.error(e)
        return abort(500)

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=9090)
