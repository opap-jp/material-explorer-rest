from flask import Flask, request, abort, Response
import subprocess

app =  Flask(__name__)

@app.route('/resize', methods=['POST'])
def resize():
    width = request.form['width']
    height = request.form['height']
    data = request.files['data']
    if (width is None or height is None or data is None):
        return abort(400)

    command = 'convert - -resize {0}x{1} png:-'.format(width, height)

    try:
        process = subprocess.Popen(command.split(" "), stdin=subprocess.PIPE, stdout=subprocess.PIPE)
        process.stdin.write(data.read())
        process.stdin.close()
        converted = process.stdout.read()
        code = process.wait()

        if (code == 0):
            return Response(converted, mimetype='image/png')
        else:
            return abort(400)
    except OSError as e:
        return abort(500)

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=9090)
