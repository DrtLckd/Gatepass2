import easyocr
import sys

# Initialize EasyOCR Reader once
reader = easyocr.Reader(['en'], gpu=False)

def perform_ocr(image_path):
    results = reader.readtext(image_path, detail=1)
    return [{"text": text, "confidence": confidence} for _, text, confidence in results]

if __name__ == "__main__":
    print("OCR Script Ready")
    sys.stdout.flush()

    while True:
        try:
            # Read image path from standard input
            image_path = sys.stdin.readline().strip()
            if image_path.lower() == "exit":
                print("Exiting OCR Script")
                break

            # Perform OCR and return results
            results = perform_ocr(image_path)
            print(results)
            sys.stdout.flush()
        except Exception as e:
            print(f"Error: {e}")
            sys.stdout.flush()
