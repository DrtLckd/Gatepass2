import easyocr
import sys
import os
import json

# Initialize EasyOCR Reader once
reader = easyocr.Reader(['en'], gpu=False)

def perform_ocr(image_path):
    if not os.path.exists(image_path):
        print(f"Error: Image path does not exist - {image_path}")
        return []
    
    results = reader.readtext(image_path, detail=1)
    
    # Return only text and confidence in a simple format
    return [{"text": text, "confidence": confidence} for _, text, confidence in results]

if __name__ == "__main__":
    # Check if the script is called with a file path as argument
    if len(sys.argv) < 2:
        print("Error: Please provide an image path")
        sys.exit(1)

    image_path = sys.argv[1]  # Get the image path from the first argument

    results = perform_ocr(image_path)
    
    # Output only the OCR results, not extra information
    print(json.dumps(results))  # Ensures valid JSON with double quotes

