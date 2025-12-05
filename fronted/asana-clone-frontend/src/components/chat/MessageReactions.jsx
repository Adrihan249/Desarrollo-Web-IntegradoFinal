// src/components/chat/MessageReactions.jsx
import { Plus } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';

const MessageReactions = ({ reactions = [], onReact }) => {
  const { user } = useAuth();

  // Group reactions by emoji
  const groupedReactions = reactions.reduce((acc, reaction) => {
    const existing = acc.find(r => r.emoji === reaction.emoji);
    if (existing) {
      existing.count++;
      existing.users.push(reaction.user);
      if (reaction.user.id === user?.id) {
        existing.userReacted = true;
      }
    } else {
      acc.push({
        emoji: reaction.emoji,
        count: 1,
        users: [reaction.user],
        userReacted: reaction.user.id === user?.id
      });
    }
    return acc;
  }, []);

  const commonEmojis = ['👍', '❤️', '😂', '🎉', '👏'];

  return (
    <div className="flex flex-wrap gap-1 mt-2">
      {groupedReactions.map((reaction, index) => (
        <button
          key={index}
          onClick={() => onReact(reaction.emoji)}
          className={`inline-flex items-center gap-1 px-2 py-1 rounded-full text-sm transition-colors ${
            reaction.userReacted
              ? 'bg-indigo-100 text-indigo-700 border-2 border-indigo-500'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          }`}
          title={reaction.users.map(u => `${u.firstName} ${u.lastName}`).join(', ')}
        >
          <span>{reaction.emoji}</span>
          <span className="text-xs font-medium">{reaction.count}</span>
        </button>
      ))}

      {/* Add Reaction */}
      <div className="relative group">
        <button className="inline-flex items-center justify-center w-8 h-8 rounded-full bg-gray-100 hover:bg-gray-200 text-gray-600">
          <Plus className="w-4 h-4" />
        </button>

        <div className="absolute bottom-full left-0 mb-2 hidden group-hover:block bg-white rounded-lg shadow-lg border p-2">
          <div className="flex gap-1">
            {commonEmojis.map(emoji => (
              <button
                key={emoji}
                onClick={() => onReact(emoji)}
                className="text-2xl hover:bg-gray-100 rounded p-1 transition-colors"
              >
                {emoji}
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default MessageReactions;