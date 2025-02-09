import json
from flask import Flask, request, jsonify
from flask_cors import CORS

app = Flask(__name__)
CORS(app)  # Permite requisições do frontend

# Carrega as perguntas do ficheiro externo questions.json quando a aplicação inicia
with open('questions.json', encoding='utf-8') as f:
    questions_data = json.load(f)

@app.route('/api/risk', methods=['POST'])
def calculate_risk():
    data = request.get_json()
    if not data:
        return jsonify({'error': 'Nenhum dado enviado.'}), 400

    try:
        # Converte os valores recebidos para float
        probabilidade = float(data.get('probabilidade', 0))
        impacto = float(data.get('impacto', 0))
    except (ValueError, TypeError):
        return jsonify({'error': 'Valores inválidos.'}), 400

    # Calcula o risco como o produto da probabilidade pelo impacto
    risco = probabilidade * impacto

    # Classifica o risco com base no valor calculado
    if risco < 5:
        classificacao = 'Baixo'
    elif risco < 15:
        classificacao = 'Médio'
    else:
        classificacao = 'Alto'

    return jsonify({
        'risco': risco,
        'classificacao': classificacao
    })

@app.route('/api/questions', methods=['GET'])
def get_questions():
    return jsonify(questions_data)

if __name__ == '__main__':
    app.run(debug=True)
