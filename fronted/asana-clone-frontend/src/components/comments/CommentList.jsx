// src/components/comments/CommentList.jsx
import { useState, useEffect } from 'react';
import { MessageSquare } from 'lucide-react';
import CommentForm from './CommentForm';
import CommentItem from './CommentItem';
import EmptyState from '../common/EmptyState';
import Spinner from '../common/Spinner';
import commentService from '../../services/commentService';

const CommentList = ({ taskId }) => {
  const [comments, setComments] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [replyingTo, setReplyingTo] = useState(null);
  const [editingComment, setEditingComment] = useState(null);

  useEffect(() => {
    loadComments();
  }, [taskId]);

  const loadComments = async () => {
    try {
      const data = await commentService.getCommentsByTask(taskId);
      setComments(data);
    } catch (error) {
      console.error('Error loading comments:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleAddComment = async (commentData) => {
    try {
      await commentService.createComment(taskId, commentData);
      await loadComments();
      setReplyingTo(null);
    } catch (error) {
      console.error('Error adding comment:', error);
    }
  };

  const handleEditComment = async (commentId, content) => {
    try {
      await commentService.updateComment(taskId, commentId, { content });
      await loadComments();
      setEditingComment(null);
    } catch (error) {
      console.error('Error editing comment:', error);
    }
  };

  const handleDeleteComment = async (commentId) => {
    if (!window.confirm('¿Estás seguro de eliminar este comentario?')) return;
    
    try {
      await commentService.deleteComment(taskId, commentId);
      await loadComments();
    } catch (error) {
      console.error('Error deleting comment:', error);
    }
  };

  // Organize comments into threads
  const organizeComments = (comments) => {
    const commentMap = new Map();
    const rootComments = [];

    // First pass: create map
    comments.forEach(comment => {
      commentMap.set(comment.id, { ...comment, replies: [] });
    });

    // Second pass: organize into threads
    comments.forEach(comment => {
      if (comment.parentCommentId) {
        const parent = commentMap.get(comment.parentCommentId);
        if (parent) {
          parent.replies.push(commentMap.get(comment.id));
        }
      } else {
        rootComments.push(commentMap.get(comment.id));
      }
    });

    return rootComments;
  };

  if (isLoading) {
    return (
      <div className="flex justify-center py-8">
        <Spinner />
      </div>
    );
  }

  const organizedComments = organizeComments(comments);

  return (
    <div className="space-y-6">
      {/* Add Comment Form */}
      <CommentForm taskId={taskId} onSubmit={handleAddComment} />

      {/* Comments List */}
      {organizedComments.length === 0 ? (
        <EmptyState
          icon={<MessageSquare className="w-12 h-12" />}
          title="No hay comentarios"
          description="Sé el primero en comentar"
        />
      ) : (
        <div className="space-y-4">
          {organizedComments.map(comment => (
            <CommentItem
              key={comment.id}
              comment={comment}
              replies={comment.replies}
              onReply={(comment) => setReplyingTo(comment)}
              onEdit={(comment) => setEditingComment(comment)}
              onDelete={handleDeleteComment}
            />
          ))}
        </div>
      )}

      {/* Reply Form */}
      {replyingTo && (
        <div className="ml-12 mt-4">
          <CommentForm
            taskId={taskId}
            parentCommentId={replyingTo.id}
            onSubmit={handleAddComment}
            onCancel={() => setReplyingTo(null)}
            placeholder={`Respondiendo a ${replyingTo.author.firstName}...`}
          />
        </div>
      )}
    </div>
  );
};

export default CommentList;