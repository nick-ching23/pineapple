from flask import Flask, request, jsonify
import Levenshtein
from openai import OpenAI

app = Flask(__name__)

client = OpenAI()

# calls ChatGPT API with given prompt.
def get_chatgpt_response(prompt: str) -> str:
    
    
    completion = client.chat.completions.create(
    model="gpt-4o",
    messages=[
        {"role": "system", "content": "Rewrite the text in the reverse only the meaning."},
        {
            "role": "user",
            "content": prompt
        }]
    )
    return completion.choices[0].message.content

# Compute Levenshtein ratio for edit distance
def levenshtein_ratio(text1: str, text2:str) -> float:
    """
    A result closer to 1: results are more similar
    A result closer to 0: results are more different
    """
    
    if len(text1) == 0 and len(text2) == 0:
        return 1.0
    
    distance = Levenshtein.distance(text1, text2)
    max_len = max(len(text1), len(text2))
    ratio = (distance / max_len)
    
    return ratio



def rewrite_text(original_text:str): 
    """
    Executes the rewrite using the equivalence method. 
    """
    first_rewrite = get_chatgpt_response(original_text)
    paraphrased_text = get_chatgpt_response(first_rewrite)

    return paraphrased_text


# Function to analyze texts based on Levenshtein ratio
def analyze_texts(original_text: str, ratio_threshold=0.65)-> bool:
    """
    Return True if written by a human 
    Return False if written by ChatGPT
    """
    
    number_rewrites = 2
    
    rewrites = [rewrite_text(original_text) for i in range(number_rewrites)]
    intermediate_ratios = 0

    for text in rewrites: 
        intermediate_ratios += levenshtein_ratio(original_text, text)
    
    
    ratio = intermediate_ratios/number_rewrites
    return True if ratio > ratio_threshold else False


@app.route('/')
def index():
    return "Hello, welcome to Veritas!"

# Flask route for executing the GPT comparison
@app.route('/api/check-text', methods=['POST'])
def check_text():
    data = request.get_json()
    original_text = data.get('text', '')

    if not original_text:
        return jsonify({"error": "No text provided"}), 400
    
    result = analyze_texts(original_text)
    
    return jsonify({"result": result})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True) 