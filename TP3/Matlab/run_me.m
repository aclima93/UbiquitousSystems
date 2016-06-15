expected = [1,1,1,1,1,1,1,1,1,2,2,3,3,3,3,3,3,3,3,3,4,4,4,4,4,4,4,4,4,4,4,4,4,4,5,5,5,5,5,5,5,6,6,6,6,6,6,6,6,6,6,6,6,6,7,7,7,7,7,7,7,7,7]';
predicted = [1,1,1,1,5,1,1,5,1,2,2,3,3,3,3,3,3,3,3,3,4,4,4,4,4,4,4,4,4,4,4,5,4,4,5,5,5,1,5,5,4,6,6,6,6,6,6,6,6,6,6,6,6,6,7,7,7,7,5,7,7,3,7]';

[confusion_matrix,order] = confusionmat(expected, predicted);

class_labels = unique(expected);

figure;
imagesc(confusion_matrix);
colorbar

ax = gca;
title('Confusion Matrix')
xlabel('Predicted Class')
ax.XTickLabel = class_labels;
ax.XTickLabelRotation = 90;
ax.XTick = 1:length(ax.XTickLabel);
ylabel('Expected Class')
ax.YTickLabel = class_labels;
ax.YTick = 1:length(ax.YTickLabel);

hold on;

h = zeros(7, 1);
h(1) = plot(0,0,'.b', 'visible', 'off');
h(2) = plot(0,0,'.b', 'visible', 'off');
h(3) = plot(0,0,'.b', 'visible', 'off');
h(4) = plot(0,0,'.b', 'visible', 'off');
h(5) = plot(0,0,'.b', 'visible', 'off');
h(6) = plot(0,0,'.b', 'visible', 'off');
h(7) = plot(0,0,'.b', 'visible', 'off');
legend(h, '1 - Casa da namorada','2 - Casa','3 - NEI','4 - Cantina','5 - Coimbra Shopping','6 - Treino','7 - Outro','Location','eastoutside');
