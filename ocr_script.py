import easyocr
import sys

def perform_ocr(image_path):
    reader = easyocr.Reader(['en'])  # Initialize EasyOCR reader
    results = reader.readtext(image_path)  # Perform OCR
    for (_, text, confidence) in results:
        # Print structured data for Java to parse
        print(f"Txt: {text}, Cfd: {confidence}")

if __name__ == "__main__":
    if len(sys.argv) > 1:
        for image_path in sys.argv[1:]:
            print(f"Processing: {image_path}")
            perform_ocr(image_path)
    else:
        print("Please provide image paths as arguments.")
