import argparse
import json

from faster_whisper import WhisperModel


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--audio", required=True)
    parser.add_argument("--model", default="tiny.en")
    parser.add_argument("--language", default="en")
    args = parser.parse_args()

    model = WhisperModel(args.model, device="cpu", compute_type="int8")
    segments, _ = model.transcribe(
        args.audio,
        language=args.language,
        beam_size=1,
        vad_filter=True,
        condition_on_previous_text=False,
    )
    transcript = " ".join(segment.text.strip() for segment in segments if segment.text.strip())
    print(json.dumps({"text": transcript}, ensure_ascii=False))


if __name__ == "__main__":
    main()
