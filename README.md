# Typerighter

<img width="1232" alt="The Typerighter checker service frontend" src="https://user-images.githubusercontent.com/7767575/103550037-353f3200-4ea0-11eb-9ba5-9e4f7ecf2d1f.png">

Typerighter is the server-side part of a service to check a document against a set of user-defined rules. It's designed to work like a spelling or grammar checker.

We use it at the Guardian to check content against our style guide. Max Walker, the subeditor who inspired the creation of Typerighter, has written an introduction [here](https://www.theguardian.com/help/insideguardian/2020/nov/20/introducing-typerighter-making-life-easier-for-journalists-and-stories-better-for-readers).

To understand our goals for the tool, see the [vision document](./vision.md).

For setup, see [the docs directory](./docs/).

For an example of a Typerighter client (the part that presents the spellcheck-style interface to the user), see [prosemirror-typerighter](https://github.com/guardian/prosemirror-typerighter).

## How it works: an overview

The Typerighter checker service ingests user-defined rules from a `RuleResource`. This is a Google sheet, but the interface could be fulfilled from an arbitrary source.

Each rule in the service corresponds to a `Matcher` that receives the document and passes back a list of `RuleMatch`. We have the following `Matcher` implementations:

- `RegexMatcher` uses regular expressions
- `LanguageToolMatcher` is powered by the [LanguageTool](https://languagetool.org/) project, and uses a combination of native LanguageTool rules and user-defined XML rules as its corpus

Matches contain the range that match applies to, a description of why the match has occurred, and any relevant suggestions – see the `RuleMatch` interface for the full description.

## Implementation

Both the checker and management services are built in Scala with the Play framework. Data is currently stored in a Google Sheet.

Google credentials are fetched from SSM using AWS Credentials or Instance Role. 

## Integration

The [prosemirror-typerighter](https://github.com/guardian/prosemirror-typerighter) plugin provides an integration for the [Prosemirror](https://prosemirror.net) rich text editor.

If you'd like to provide your own integration, this service will function as a standalone REST platform, but you'll need to use [pan-domain-authentication](https://github.com/guardian/pan-domain-authentication) to provide a valid auth cookie with your requests.

## Upgrading LanguageTool

LanguageTool has default rules that we use, and as we upgrade LT, these could change underneath us.

There's a script to see if rules have changed as a result of an upgrade in ./script/js/compare-rule-xml.js.
