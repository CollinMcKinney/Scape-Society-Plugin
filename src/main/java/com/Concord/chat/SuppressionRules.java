package com.concord.chat;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SuppressionRules
{
	private List<String> rules = Collections.emptyList();

	public void setRules(List<String> rules)
	{
		this.rules = rules != null ? rules : Collections.emptyList();
	}

	public void clear()
	{
		this.rules = Collections.emptyList();
	}

	public int size()
	{
		return rules.size();
	}

	public boolean isSuppressed(String message)
	{
		if (rules.isEmpty())
		{
			return false;
		}

		String normalizedMessage = normalizeSuppressionValue(message);
		for (String rule : rules)
		{
			if (doesSuppressionRuleMatch(normalizedMessage, rule))
			{
				return true;
			}
		}

		return false;
	}

	private String normalizeSuppressionValue(String value)
	{
		return value == null ? "" : value.replaceAll("<[^>]+>", "").trim();
	}

	private boolean doesSuppressionRuleMatch(String normalizedMessage, String rule)
	{
		String normalizedRule = rule == null ? "" : rule.trim();
		if (normalizedRule.isEmpty())
		{
			return false;
		}

		Pattern regex = parseSuppressionRegex(normalizedRule);
		if (regex != null)
		{
			return regex.matcher(normalizedMessage).find();
		}

		return normalizedMessage.contains(normalizeSuppressionValue(normalizedRule));
	}

	private Pattern parseSuppressionRegex(String rule)
	{
		Matcher matcher = Pattern.compile("^/(.*)/([dgimsuy]*)$").matcher(rule);
		if (!matcher.matches())
		{
			return null;
		}

		try
		{
			return Pattern.compile(matcher.group(1), parseJavaRegexFlags(matcher.group(2)));
		}
		catch (PatternSyntaxException ex)
		{
			log.warn("Ignoring invalid suppression regex {}: {}", rule, ex.getMessage());
			return null;
		}
	}

	private int parseJavaRegexFlags(String flags)
	{
		int compiledFlags = 0;
		for (char flag : flags.toCharArray())
		{
			switch (flag)
			{
				case 'i':
					compiledFlags |= Pattern.CASE_INSENSITIVE;
					break;
				case 'm':
					compiledFlags |= Pattern.MULTILINE;
					break;
				case 's':
					compiledFlags |= Pattern.DOTALL;
					break;
				case 'u':
					compiledFlags |= Pattern.UNICODE_CASE;
					break;
				case 'd':
				case 'g':
				case 'y':
					break;
				default:
					log.warn("Ignoring unsupported suppression regex flag: {}", flag);
			}
		}

		return compiledFlags;
	}
}
