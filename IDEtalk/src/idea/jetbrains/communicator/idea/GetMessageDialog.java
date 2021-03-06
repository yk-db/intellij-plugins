/*
 * Copyright 2000-2006 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.communicator.idea;

import jetbrains.communicator.util.HardWrapUtil;

import javax.swing.*;

/**
 * @author Kir
 */
public class GetMessageDialog extends IdeaDialog {
  private JLabel myLabel;
  private JTextArea myTextArea;
  private JPanel myPanel;
  private final HardWrapUtil myWrapper;

  public GetMessageDialog(String titleText, String labelText, String optionalOKButtonText) {
    super(false);
    setModal(true);
    setTitle(titleText);
    myLabel.setText(labelText);
    
    if (optionalOKButtonText != null) {
      setOKButtonText(optionalOKButtonText);
    }

    myWrapper = new HardWrapUtil(myTextArea);
    init();
  }

  public JComponent getPreferredFocusedComponent() {
    return myTextArea;
  }

  protected JComponent createCenterPanel() {
    return myPanel;
  }

  public String getEnteredText() {
    return isOK() ? myWrapper.getText() : null;
  }

}
