import subprocess


test_cases = [
    {
        'input': 'first_w',
        'expected_output': 'first_word_explanation',
        'expected_error': ''
    },
    {
        'input': 'second_w',
        'expected_output': 'second_word_explanation',
        'expected_error': ''
    },
    {
        'input': 'neko',
        'expected_output': 'NekoPeko',
        'expected_error': ''
    },
    {
        'input': 'peko',
        'expected_output': '',
        'expected_error': 'String not found.'
    },
    {
        'input': 'a b c',
        'expected_output': 'abc',
        'expected_error': ''
    },
    {
        'input': 'peko' * 100,
        'expected_output': '',
        'expected_error': 'String is too long (>255).'
    }
]

for test_case in test_cases:
    process = subprocess.run("./program", input=test_case['input'], text=True, capture_output=True)

    if (
        process.stdout != test_case['expected_output']
        or process.stderr != test_case['expected_error']
    ):
        print(f"[ERROR]")
        print("Input:", repr(test_case['input']))
        print("Expected Output:", repr(test_case['expected_output']))
        print("Expected Error:", repr(test_case['expected_error']))
        print("Actual Output:", repr(process.stdout))
        print("Actual Error:", repr(process.stderr), end='\n\n')

    else:
        print(f"[OK] {test_case['input']}")
