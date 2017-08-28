
import json
from watson_developer_cloud import NaturalLanguageUnderstandingV1
import watson_developer_cloud.natural_language_understanding.features.v1 as \
    features

def nlu_api(text_to_analyze = 'None', user=None, passw=None):
    natural_language_understanding = NaturalLanguageUnderstandingV1(
        version='2017-02-27', username=user,
        password=passw)
    """
    response = natural_language_understanding.analyze(
        text=text_to_analyze,
            features=[features.Entities(sentiment=True),
                      features.Keywords(),
                      features.Sentiment(),
                      features.Emotion()])
    """
    response = natural_language_understanding.analyze(
        text=text_to_analyze,
        features=
        #[features.Keywords()]
        [#features.Entities(sentiment=True)]
         features.Sentiment()]
    )

    #print(json.dumps(response, indent=2))
    return json.dumps(response, indent=2)