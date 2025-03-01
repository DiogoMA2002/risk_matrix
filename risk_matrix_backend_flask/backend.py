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
        # Calcula o risco geral
        overall_prob = float(data.get("probabilidade", 0))
        overall_impact = float(data.get("impacto", 0))
    except (ValueError, TypeError):
        return jsonify({'error': 'Valores inválidos para o risco geral.'}), 400

    overall_risk = overall_prob * overall_impact
    if overall_risk < 5:
        overall_class = 'Baixo'
    elif overall_risk < 15:
        overall_class = 'Médio'
    else:
        overall_class = 'Alto'

    # Calcula o risco para cada categoria, se fornecida
    categories_input = data.get("categorias", {})
    categories_results = {}

    for category, values in categories_input.items():
        try:
            cat_prob = float(values.get("probabilidade", 0))
            cat_impact = float(values.get("impacto", 0))
        except (ValueError, TypeError):
            cat_prob, cat_impact = 0, 0
        cat_risk = cat_prob * cat_impact
        if cat_risk < 5:
            cat_class = 'Baixo'
        elif cat_risk < 15:
            cat_class = 'Médio'
        else:
            cat_class = 'Alto'
        categories_results[category] = {
            "risco": cat_risk,
            "classificacao": cat_class
        }

    return jsonify({
        "risco_geral": overall_risk,
        "classificacao_geral": overall_class,
        "categorias": categories_results
    })

@app.route('/api/questions', methods=['GET'])
def get_questions():
    return jsonify(questions_data)

if __name__ == '__main__':
    app.run(debug=True)
